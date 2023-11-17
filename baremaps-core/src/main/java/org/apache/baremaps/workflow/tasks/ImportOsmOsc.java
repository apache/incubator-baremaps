/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.baremaps.database.collection.*;
import org.apache.baremaps.database.memory.MemoryMappedDirectory;
import org.apache.baremaps.database.type.LongDataType;
import org.apache.baremaps.database.type.LongListDataType;
import org.apache.baremaps.database.type.PairDataType;
import org.apache.baremaps.database.type.geometry.LonLatDataType;
import org.apache.baremaps.openstreetmap.function.*;
import org.apache.baremaps.openstreetmap.postgres.*;
import org.apache.baremaps.openstreetmap.repository.CopyChangeImporter;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.utils.Compression;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import an OSM OSC file into a database.
 */
@JsonTypeName("ImportOsmOsc")
public class ImportOsmOsc implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOsmOsc.class);

  private Path file;
  private Path cache;
  private Object database;
  private Integer srid;
  private Compression compression;

  /**
   * Constructs an {@code ImportOsmOsc}.
   *
   * @param file the OSM OSC file
   * @param cache the cache directory
   * @param database the database
   * @param srid the database SRID
   * @param compression the compression
   */
  public ImportOsmOsc(Path file, Path cache, Object database, Integer srid,
      Compression compression) {
    this.file = file;
    this.cache = cache;
    this.database = database;
    this.srid = srid;
    this.compression = compression;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var datasource = context.getDataSource(database);
    var path = file.toAbsolutePath();

    var cacheDir = cache != null ? cache : Files.createTempDirectory(Paths.get("."), "cache_");

    DataMap<Long, Coordinate> coordinateMap;
    if (Files.size(path) > 1 << 30) {
      var coordinateDir = Files.createDirectories(cacheDir.resolve("coordinates"));
      coordinateMap = new MemoryAlignedDataMap<>(
          new LonLatDataType(),
          new MemoryMappedDirectory(coordinateDir));
    } else {
      var coordinateKeysDir = Files.createDirectories(cacheDir.resolve("coordinate_keys"));
      var coordinateValuesDir = Files.createDirectories(cacheDir.resolve("coordinate_vals"));
      coordinateMap =
          new MonotonicDataMap<>(
              new MemoryAlignedDataList<>(
                  new PairDataType<>(new LongDataType(), new LongDataType()),
                  new MemoryMappedDirectory(coordinateKeysDir)),
              new AppendOnlyBuffer<>(
                  new LonLatDataType(),
                  new MemoryMappedDirectory(coordinateValuesDir)));
    }

    var referenceKeysDir = Files.createDirectories(cacheDir.resolve("reference_keys"));
    var referenceValuesDir = Files.createDirectories(cacheDir.resolve("reference_vals"));
    var referenceMap =
        new MonotonicDataMap<>(
            new MemoryAlignedDataList<>(
                new PairDataType<>(new LongDataType(), new LongDataType()),
                new MemoryMappedDirectory(referenceKeysDir)),
            new AppendOnlyBuffer<>(
                new LongListDataType(),
                new MemoryMappedDirectory(referenceValuesDir)));

    var nodeRepository = new PostgresNodeRepository(datasource);
    var wayRepository = new PostgresWayRepository(datasource);
    var relationRepository = new PostgresRelationRepository(datasource);

    var coordinateMapBuilder = new CoordinateMapBuilder(coordinateMap);
    var referenceMapBuilder = new ReferenceMapBuilder(referenceMap);
    var buildGeometry = new EntityGeometryBuilder(coordinateMap, referenceMap);
    var reprojectGeometry = new EntityProjectionTransformer(4326, srid);
    var prepareGeometries = coordinateMapBuilder
        .andThen(referenceMapBuilder)
        .andThen(buildGeometry)
        .andThen(reprojectGeometry);
    var prepareChange = consumeThenReturn(new ChangeEntitiesHandler(prepareGeometries));
    var importChange = new CopyChangeImporter(nodeRepository, wayRepository, relationRepository);

    try (var changeInputStream =
        new BufferedInputStream(compression.decompress(Files.newInputStream(path)))) {
      new XmlChangeReader().stream(changeInputStream).map(prepareChange).forEach(importChange);
    }

    FileUtils.deleteRecursively(cacheDir);
  }
}
