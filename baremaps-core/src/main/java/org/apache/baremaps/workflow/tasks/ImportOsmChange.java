/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.workflow.tasks;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

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
import org.apache.baremaps.openstreetmap.repository.ChangeImporter;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.utils.Compression;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportOsmChange(Path file, Object database, Integer srid,
    Compression compression) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOsmChange.class);

  public ImportOsmChange(Path file, Object database, Integer srid) {
    this(file, database, srid, Compression.detect(file));
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var datasource = context.getDataSource(database);
    var path = file.toAbsolutePath();

    var cacheDir = Files.createTempDirectory(Paths.get("."), "cache_");

    DataMap<Long, Coordinate> coordinateMap;
    if (Files.size(path) > 1 << 30) {
      var coordinateDir = Files.createDirectories(cacheDir.resolve("coordinate_keys"));
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

    var referenceKeysDir = Files.createDirectory(cacheDir.resolve("reference_keys"));
    var referenceValuesDir = Files.createDirectory(cacheDir.resolve("reference_vals"));
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
    var prepareGeometries = coordinateMapBuilder.andThen(referenceMapBuilder).andThen(buildGeometry)
        .andThen(reprojectGeometry);
    var prepareChange = consumeThenReturn(new ChangeEntitiesHandler(prepareGeometries));
    var importChange = new ChangeImporter(nodeRepository, wayRepository, relationRepository);

    try (var changeInputStream =
        new BufferedInputStream(compression.decompress(Files.newInputStream(path)))) {
      new XmlChangeReader().stream(changeInputStream).map(prepareChange).forEach(importChange);
    }

    FileUtils.deleteRecursively(cacheDir);
  }
}
