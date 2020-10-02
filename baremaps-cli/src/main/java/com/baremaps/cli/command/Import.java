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

package com.baremaps.cli.command;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.InMemoryCache;
import com.baremaps.osm.cache.LmdbCoordinateCache;
import com.baremaps.osm.cache.LmdbReferencesCache;
import com.baremaps.osm.parser.PBFFileBlockGeometryParser;
import com.baremaps.osm.store.PostgisHeaderStore;
import com.baremaps.osm.store.PostgisNodeStore;
import com.baremaps.osm.store.PostgisRelationStore;
import com.baremaps.osm.store.PostgisWayStore;
import com.baremaps.osm.store.StoreImportHandler;
import com.baremaps.util.postgis.PostgisHelper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
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

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(mixins.logLevel.name()));
    logger.info("{} processors available", Runtime.getRuntime().availableProcessors());
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    logger.info("Initializing database");
    executeStatements("osm_drop_tables.sql", datasource);
    executeStatements("osm_create_tables.sql", datasource);
    executeStatements("osm_create_primary_keys.sql", datasource);

    logger.info("Fetching data");
    Path path = mixins.blobStore().fetch(input);

    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem sourceCRS = crsFactory.createFromName("EPSG:4326");
    CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:3857");
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    CoordinateTransform coordinateTransform = coordinateTransformFactory
        .createTransform(sourceCRS, targetCRS);
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);

    PostgisHeaderStore headerTable = new PostgisHeaderStore(datasource);
    PostgisNodeStore nodeStore = new PostgisNodeStore(datasource);
    PostgisWayStore wayStore = new PostgisWayStore(datasource);
    PostgisRelationStore relationStore = new PostgisRelationStore(datasource);
    StoreImportHandler storeImportHandler = new StoreImportHandler(headerTable, nodeStore, wayStore,
        relationStore);

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
    PBFFileBlockGeometryParser parser = new PBFFileBlockGeometryParser(
        geometryFactory, coordinateTransform, coordinateCache, referencesCache);
    parser.parse(path, storeImportHandler);

    logger.info("Indexing geometries");
    executeStatements("osm_create_gist_indexes.sql", datasource);

    logger.info("Indexing attributes");
    executeStatements("osm_create_gin_indexes.sql", datasource);

    return 0;
  }

  public void executeStatements(String path, DataSource datasource) throws IOException {
    URL url = Resources.getResource(path);
    String sql = Resources.toString(url, Charsets.UTF_8);
    try (Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
