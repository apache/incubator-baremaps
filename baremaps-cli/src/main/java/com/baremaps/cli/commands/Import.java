package com.baremaps.cli.commands;

import static org.lmdbjava.DbiFlags.MDB_CREATE;

import com.baremaps.core.io.InputStreams;
import com.baremaps.core.stream.BatchSpliterator;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.osmpbf.FileBlock;
import com.baremaps.osm.osmpbf.FileBlockSpliterator;
import com.baremaps.osm.postgis.PostgisHeaderStore;
import com.baremaps.osm.store.Store;
import com.baremaps.osm.store.StoreConsumer;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.osm.cache.CacheConsumer;
import com.baremaps.osm.cache.LmdbCoordinateCache;
import com.baremaps.osm.cache.LmdbReferenceCache;
import com.baremaps.osm.postgis.PostgisNodeStore;
import com.baremaps.osm.postgis.PostgisRelationStore;
import com.baremaps.osm.postgis.PostgisWayStore;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "import")
public class Import implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Import.class);

  @Parameters(
      index = "0",
      paramLabel = "PBF_FILE",
      description = "The PBF file.")
  private String source;

  @Parameters(
      index = "1",
      paramLabel = "POSTGRES_DATABASE",
      description = "The postgres database.")
  private String database;

  @Override
  public Integer call() throws Exception {
    logger.info("{} processors available.", Runtime.getRuntime().availableProcessors());

    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    logger.info("Dropping tables.");
    loadStatements("osm_drop_tables.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    logger.info("Creating tables.");
    loadStatements("osm_create_tables.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    logger.info("Creating primary keys.");
    loadStatements("osm_create_primary_keys.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    Path lmdbPath = Files.createTempDirectory("baremaps_");
    Env<ByteBuffer> env = Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(lmdbPath.toFile());
    Store<Long, Coordinate> coordinateStore = new LmdbCoordinateCache(env, env.openDbi("coordinates", MDB_CREATE));
    Store<Long, List<Long>> referenceStore = new LmdbReferenceCache(env, env.openDbi("references", MDB_CREATE));

    logger.info("Populating cache.");
    try (InputStream input = InputStreams.from(source)) {
      Stream<FileBlock> blocks = StreamSupport.stream(new FileBlockSpliterator(new DataInputStream(input)), false);
      CacheConsumer blockConsumer = new CacheConsumer(coordinateStore, referenceStore);
      blocks.forEach(blockConsumer);
    }

    logger.info("Populating database.");
    try (InputStream input = InputStreams.from(source)) {
      Stream<FileBlock> blocks = StreamSupport
          .stream(new BatchSpliterator<>(new FileBlockSpliterator(new DataInputStream(input)), 10), true);
      CRSFactory crsFactory = new CRSFactory();
      CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
      CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
      CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
      CoordinateTransform coordinateTransform = coordinateTransformFactory.createTransform(epsg4326, epsg3857);
      GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
      PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
      PostgisNodeStore nodeMapper = new PostgisNodeStore(datasource,
          new NodeBuilder(coordinateTransform, geometryFactory));
      PostgisWayStore wayMapper = new PostgisWayStore(datasource,
          new WayBuilder(coordinateTransform, geometryFactory, coordinateStore));
      PostgisRelationStore relationMapper = new PostgisRelationStore(datasource,
          new RelationBuilder(coordinateTransform, geometryFactory, coordinateStore, referenceStore));
      StoreConsumer blockConsumer = new StoreConsumer(headerMapper, nodeMapper, wayMapper, relationMapper);
      blocks.forEach(blockConsumer);
    }

    logger.info("Indexing geometries.");
    loadStatements("osm_create_gist_indexes.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    logger.info("Indexing attributes.");
    loadStatements("osm_create_gin_indexes.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
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
