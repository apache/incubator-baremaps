/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.cli;

import com.baremaps.importer.ImportTask;
import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.cache.InMemoryCache;
import com.baremaps.importer.cache.LmdbCoordinateCache;
import com.baremaps.importer.cache.LmdbReferencesCache;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.WayTable;
import com.baremaps.util.postgis.PostgisHelper;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "import", description = "Import OpenStreetMap data in the Postgresql database.")
public class Import implements Callable<Integer> {

  private static Logger logger = LogManager.getLogger();

  private enum CacheType {
    lmdb, inmemory
  }

  @Mixin
  private Options mixins;

  @Option(
      names = {"--input"},
      paramLabel = "PBF",
      description = "The OpenStreetMap PBF file.",
      required = true)
  private URI input;

  @Option(
      names = {"--database"},
      paramLabel = "JDBC",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--cache-type"},
      paramLabel = "CACHE_TYPE",
      description = "The type of cache to be used when importing data.")
  private CacheType cacheType = CacheType.lmdb;

  @Option(
      names = {"--cache-directory"},
      paramLabel = "CACHE_DIRECTORY",
      description = "The directory used by the cache data.")
  private Path cacheDirectory;

  @Option(
      names = {"--drop-tables"},
      paramLabel = "DROP_TABLES",
      description = "Drop the OpenStreetMap tables.")
  private boolean dropTables = true;

  @Option(
      names = {"--create-tables"},
      paramLabel = "CREATE_TABLES",
      description = "Create the OpenStreetMap tables.")
  private boolean createTables = true;

  @Option(
      names = {"--truncate-tables"},
      paramLabel = "TRUNCATE_TABLES",
      description = "Truncate the OpenStreetMap tables.")
  private boolean truncateTables = false;

  @Option(
      names = {"--create-gist-indexes"},
      paramLabel = "CREATE_GIST_INDEXES",
      description = "Index the geometries with GIST indexes.")
  private boolean createGistIndexes = true;

  @Option(
      names = {"--create-spgist-indexes"},
      paramLabel = "CREATE_SPGIST_INDEXES",
      description = "Index the geometries with SPGIST indexes.")
  private boolean createSpGistIndexes = false;

  @Option(
      names = {"--create-gin-indexes"},
      paramLabel = "CREATE_GIN_INDEXES",
      description = "Index the attributes with GIN indexes.")
  private boolean createGinIndexes = true;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(mixins.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    if (dropTables) {
      logger.info("Dropping tables");
      PostgisHelper.executeParallel(datasource, "osm_drop_tables.sql");
    }

    if (truncateTables) {
      logger.info("Truncating tables");
      PostgisHelper.executeParallel(datasource, "osm_truncate_tables.sql");
    }

    if (createTables) {
      logger.info("Creating tables");
      PostgisHelper.executeParallel(datasource, "osm_create_tables.sql");
    }

    HeaderTable headerTable = new HeaderTable(datasource);
    NodeTable nodeTable = new NodeTable(datasource);
    WayTable wayTable = new WayTable(datasource);
    RelationTable relationTable = new RelationTable(datasource);

    final Cache<Long, Coordinate> coordinateCache;
    final Cache<Long, List<Long>> referenceCache;
    switch (cacheType) {
      case inmemory:
        coordinateCache = new InMemoryCache<>();
        referenceCache = new InMemoryCache<>();
        break;
      case lmdb:
        if (cacheDirectory != null) {
          cacheDirectory = Files.createDirectories(cacheDirectory);
        } else {
          cacheDirectory = Files.createTempDirectory("baremaps_");
        }
        Env<ByteBuffer> env = Env.create()
            .setMapSize(1_000_000_000_000L)
            .setMaxDbs(3)
            .open(cacheDirectory.toFile());
        coordinateCache = new LmdbCoordinateCache(env);
        referenceCache = new LmdbReferencesCache(env);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported cache type");
    }

    new ImportTask(
        input,
        mixins.blobStore(),
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).execute();

    logger.info("Indexing geometries");
    if (createGistIndexes) {
      PostgisHelper.executeParallel(datasource, "osm_create_gist_indexes.sql");
    }
    if (createSpGistIndexes) {
      PostgisHelper.executeParallel(datasource, "osm_create_spgist_indexes.sql");
    }
    if (createGinIndexes) {
      logger.info("Indexing attributes");
      PostgisHelper.executeParallel(datasource, "osm_create_gin_indexes.sql");
    }

    return 0;
  }

}
