/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.cli.commands;

import static org.lmdbjava.DbiFlags.MDB_CREATE;

import com.baremaps.core.fetch.Fetcher;
import com.baremaps.core.fetch.Data;
import com.baremaps.core.stream.BatchSpliterator;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.osmpbf.FileBlock;
import com.baremaps.osm.osmpbf.FileBlockSpliterator;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.stream.DatabaseImporter;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.osm.stream.CacheImporter;
import com.baremaps.osm.cache.LmdbCoordinateCache;
import com.baremaps.osm.cache.LmdbReferenceCache;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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

  @Mixin
  private Mixins mixins;

  @Option(
      names = {"--input"},
      paramLabel = "PBF",
      description = "The OpenStreetMap PBF file.",
      required = true)
  private String input;

  @Option(
      names = {"--database"},
      paramLabel = "JDBC",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Override
  public Integer call() throws Exception {
    Configurator.setRootLevel(Level.getLevel(mixins.level));
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);
    logger.info("Dropping tables.");
    loadStatements("osm_drop_tables.sql").forEach(query -> {
      try (Connection connection = datasource.getConnection();
          Statement statement = connection.createStatement()) {
        statement.execute(query);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    logger.info("Creating tables.");
    loadStatements("osm_create_tables.sql").forEach(query -> {
      try (Connection connection = datasource.getConnection();
          Statement statement = connection.createStatement()) {
        statement.execute(query);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    logger.info("Creating primary keys.");
    loadStatements("osm_create_primary_keys.sql").forEach(query -> {
      try (Connection connection = datasource.getConnection();
          Statement statement = connection.createStatement()) {
        statement.execute(query);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    Path lmdbPath = Files.createTempDirectory("baremaps_");
    Env<ByteBuffer> env = Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(lmdbPath.toFile());
    Cache<Long, Coordinate> coordinateCache = new LmdbCoordinateCache(env,
        env.openDbi("coordinates", MDB_CREATE));
    Cache<Long, List<Long>> referenceCache = new LmdbReferenceCache(env,
        env.openDbi("references", MDB_CREATE));

    logger.info("Fetching input.");
    Fetcher fetcher = new Fetcher(mixins.caching);
    Data fetch = fetcher.fetch(this.input);

    logger.info("Populating cache.");
    try (DataInputStream input = new DataInputStream(fetch.getInputStream())) {
      Stream<FileBlock> blocks = StreamSupport.stream(new FileBlockSpliterator(input), false);
      CacheImporter blockConsumer = new CacheImporter(coordinateCache, referenceCache);
      blocks.forEach(blockConsumer);
    }

    logger.info("Populating database.");
    try (DataInputStream input = new DataInputStream(fetch.getInputStream())) {
      Stream<FileBlock> blocks = StreamSupport
          .stream(new BatchSpliterator<>(new FileBlockSpliterator(input), 10), true);
      CRSFactory crsFactory = new CRSFactory();
      CoordinateReferenceSystem sourceCRS = crsFactory.createFromName("EPSG:4326");
      CoordinateReferenceSystem targetCSR = crsFactory.createFromName("EPSG:3857");
      CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
      CoordinateTransform coordinateTransform = coordinateTransformFactory
          .createTransform(sourceCRS, targetCSR);
      GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
      HeaderTable headerMapper = new HeaderTable(datasource);

      NodeBuilder nodeBuilder = new NodeBuilder(coordinateTransform, geometryFactory);
      WayBuilder wayBuilder = new WayBuilder(coordinateTransform, geometryFactory, coordinateCache);
      RelationBuilder relationBuilder = new RelationBuilder(coordinateTransform, geometryFactory,
          coordinateCache, referenceCache);

      NodeTable nodeTable = new NodeTable(datasource);
      WayTable wayTable = new WayTable(datasource);
      RelationTable relationTable = new RelationTable(datasource);

      DatabaseImporter blockConsumer = new DatabaseImporter(headerMapper, nodeBuilder, wayBuilder,
          relationBuilder, nodeTable, wayTable, relationTable);

      blocks.forEach(blockConsumer);
    }

    logger.info("Indexing geometries.");
    loadStatements("osm_create_gist_indexes.sql").forEach(query -> {
      try (Connection connection = datasource.getConnection();
          Statement statement = connection.createStatement()) {
        statement.execute(query);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    logger.info("Indexing attributes.");
    loadStatements("osm_create_gin_indexes.sql").forEach(query -> {
      try (Connection connection = datasource.getConnection();
          Statement statement = connection.createStatement()) {
        statement.execute(query);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    return 0;
  }

  public Stream<String> loadStatements(String path) throws IOException {
    URL url = Resources.getResource(path);
    String sql = Resources.toString(url, Charsets.UTF_8);
    return Stream.of(sql.split(";")).map(statement -> statement.trim()).parallel();
  }

}
