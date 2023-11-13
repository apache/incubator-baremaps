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

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.baremaps.database.collection.*;
import org.apache.baremaps.database.memory.MemoryMappedDirectory;
import org.apache.baremaps.database.type.LongDataType;
import org.apache.baremaps.database.type.LongListDataType;
import org.apache.baremaps.database.type.PairDataType;
import org.apache.baremaps.database.type.geometry.LonLatDataType;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.openstreetmap.postgres.PostgresHeaderRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.apache.baremaps.openstreetmap.repository.*;
import org.apache.baremaps.openstreetmap.repository.BlockImporter;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import an OSM PBF file into a database.
 */
@JsonTypeName("ImportOsmPbf")
public class ImportOsmPbf implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportOsmPbf.class);

  private Path file;
  private Path cache;
  private Boolean cleanCache;
  private Object database;
  private Integer databaseSrid;
  private Boolean replaceExisting;

  /**
   * Constructs an {@code ImportOsmPbf}.
   */
  public ImportOsmPbf() {

  }

  /**
   * Constructs an {@code ImportOsmPbf}.
   *
   * @param file the OSM PBF file
   * @param cache the cache directory
   * @param cleanCache whether to clean the cache directory
   * @param database the database
   * @param databaseSrid the database SRID
   * @param replaceExisting whether to replace the existing tables
   */
  public ImportOsmPbf(Path file, Path cache, Boolean cleanCache, Object database,
      Integer databaseSrid, Boolean replaceExisting) {
    this.file = file;
    this.cache = cache;
    this.cleanCache = cleanCache;
    this.database = database;
    this.databaseSrid = databaseSrid;
    this.replaceExisting = replaceExisting;
  }

  /**
   * Returns the OSM PBF file.
   *
   * @return the OSM PBF file
   */
  public Path getFile() {
    return file;
  }

  /**
   * Sets the OSM PBF file.
   *
   * @param file the OSM PBF file
   */
  public void setFile(Path file) {
    this.file = file;
  }

  /**
   * Returns the cache directory.
   *
   * @return the cache directory
   */
  public Path getCache() {
    return cache;
  }

  /**
   * Sets the cache directory.
   *
   * @param cache the cache directory
   */
  public void setCache(Path cache) {
    this.cache = cache;
  }

  /**
   * Returns whether to clean the cache directory.
   *
   * @return whether to clean the cache directory
   */
  public Boolean getCleanCache() {
    return cleanCache;
  }

  /**
   * Sets whether to clean the cache directory.
   *
   * @param cleanCache whether to clean the cache directory
   */
  public void setCleanCache(Boolean cleanCache) {
    this.cleanCache = cleanCache;
  }

  /**
   * Returns the database.
   *
   * @return the database
   */
  public Object getDatabase() {
    return database;
  }

  /**
   * Sets the database.
   *
   * @param database the database
   */
  public void setDatabase(Object database) {
    this.database = database;
  }

  /**
   * Returns the database SRID.
   *
   * @return the database SRID
   */
  public Integer getDatabaseSrid() {
    return databaseSrid;
  }

  /**
   * Sets the database SRID.
   *
   * @param databaseSrid the database SRID
   */
  public void setDatabaseSrid(Integer databaseSrid) {
    this.databaseSrid = databaseSrid;
  }

  /**
   * Returns whether to replace the existing tables.
   *
   * @return whether to replace the existing tables
   */
  public Boolean getReplaceExisting() {
    return replaceExisting;
  }

  /**
   * Sets whether to replace the existing tables.
   *
   * @param replaceExisting whether to replace the existing tables
   */
  public void setReplaceExisting(Boolean replaceExisting) {
    this.replaceExisting = replaceExisting;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var dataSource = context.getDataSource(database);
    var path = file.toAbsolutePath();

    var headerRepository = new PostgresHeaderRepository(dataSource);
    var nodeRepository = new PostgresNodeRepository(dataSource);
    var wayRepository = new PostgresWayRepository(dataSource);
    var relationRepository = new PostgresRelationRepository(dataSource);

    if (replaceExisting) {
      headerRepository.drop();
      nodeRepository.drop();
      wayRepository.drop();
      relationRepository.drop();
      headerRepository.create();
      nodeRepository.create();
      wayRepository.create();
      relationRepository.create();
    }

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

    execute(
        path,
        coordinateMap,
        referenceMap,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        databaseSrid);

    if (cleanCache) {
      FileUtils.deleteRecursively(cacheDir);
    }
  }

  /**
   * Imports an OSM PBF file into a database.
   *
   * @param path the OSM PBF file
   * @param coordinateMap the coordinate map
   * @param referenceMap the reference map
   * @param headerRepository the header repository
   * @param nodeRepository the node repository
   * @param wayRepository the way repository
   * @param relationRepository the relation repository
   * @param databaseSrid the database SRID
   * @throws IOException
   */
  public static void execute(
      Path path,
      DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap,
      HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository,
      Integer databaseSrid) throws IOException {

    // configure the block reader
    var reader = new PbfBlockReader()
        .geometries(true)
        .projection(databaseSrid)
        .coordinateMap(coordinateMap)
        .referenceMap(referenceMap);

    // configure the block importer
    var importer = new BlockImporter(
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository);

    // Stream and process the blocks
    try (var input = Files.newInputStream(path)) {
      StreamUtils.batch(reader.stream(input)).forEach(importer);
    }
  }
}
