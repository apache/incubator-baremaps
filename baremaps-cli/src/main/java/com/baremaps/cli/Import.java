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

import com.baremaps.importer.cache.LmdbCoordinateCache;
import com.baremaps.importer.cache.LmdbReferencesCache;
import com.baremaps.importer.database.HeaderTable;
import com.baremaps.importer.database.ImportHandler;
import com.baremaps.importer.database.NodeTable;
import com.baremaps.importer.database.RelationTable;
import com.baremaps.importer.database.WayTable;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.InMemoryCache;
import com.baremaps.osm.reader.pbf.FileBlockGeometryReader;
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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
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
  private Mixins mixins;

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

    logger.info("Fetching data");
    Path path = mixins.blobStore().fetch(input);

    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem sourceCRS = crsFactory.createFromName("EPSG:4326");
    CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:3857");
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    CoordinateTransform coordinateTransform = coordinateTransformFactory
        .createTransform(sourceCRS, targetCRS);
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);

    HeaderTable headerTable = new HeaderTable(datasource);
    NodeTable nodeStore = new NodeTable(datasource);
    WayTable wayStore = new WayTable(datasource);
    RelationTable relationStore = new RelationTable(datasource);
    ImportHandler importHandler = new ImportHandler(headerTable, nodeStore, wayStore, relationStore);

    final Cache<Long, Coordinate> coordinateCache;
    final Cache<Long, List<Long>> referencesCache;
    switch (cacheType) {
      case inmemory:

        coordinateCache = new InMemoryCache<>();
        referencesCache = new InMemoryCache<>();
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
        referencesCache = new LmdbReferencesCache(env);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported cache type");
    }

    logger.info("Importing data");
    FileBlockGeometryReader parser = new FileBlockGeometryReader(
        geometryFactory, coordinateTransform, coordinateCache, referencesCache);
    parser.read(path, importHandler);

    if (createGistIndexes) {
      logger.info("Indexing geometries (GIST)");
      PostgisHelper.executeParallel(datasource, "osm_create_gist_indexes.sql");
    }

    if (createSpGistIndexes) {
      logger.info("Indexing geometries (SPGIST)");
      PostgisHelper.executeParallel(datasource, "osm_create_spgist_indexes.sql");
    }

    if (createGinIndexes) {
      logger.info("Indexing attributes (GIN)");
      PostgisHelper.executeParallel(datasource, "osm_create_gin_indexes.sql");
    }

    return 0;
  }


}
