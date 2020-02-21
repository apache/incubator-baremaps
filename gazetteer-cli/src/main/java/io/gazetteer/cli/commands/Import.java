package io.gazetteer.cli.commands;

import static io.gazetteer.cli.util.IOUtil.input;
import static io.gazetteer.cli.util.IOUtil.url;
import static org.lmdbjava.DbiFlags.MDB_CREATE;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import io.gazetteer.osm.cache.CacheConsumer;
import io.gazetteer.osm.cache.LmdbCoordinateCache;
import io.gazetteer.osm.cache.LmdbReferenceCache;
import io.gazetteer.osm.postgis.PostgisHeaderStore;
import io.gazetteer.core.postgis.PostgisHelper;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import io.gazetteer.osm.postgis.PostgisRelationStore;
import io.gazetteer.osm.postgis.PostgisWayStore;
import io.gazetteer.osm.geometry.NodeBuilder;
import io.gazetteer.osm.geometry.RelationBuilder;
import io.gazetteer.osm.geometry.WayBuilder;
import io.gazetteer.osm.osmpbf.FileBlock;
import io.gazetteer.osm.osmpbf.FileBlockSpliterator;
import io.gazetteer.osm.store.Store;
import io.gazetteer.osm.store.StoreConsumer;
import io.gazetteer.core.stream.BatchSpliterator;
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
import java.util.concurrent.TimeUnit;
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
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "import")
public class Import implements Callable<Integer> {

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

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Override
  public Integer call() throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    PoolingDataSource datasource = PostgisHelper.poolingDataSource(database);

    System.out.println("Dropping tables.");
    loadStatements("osm_drop_tables.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
    System.out.println(String.format("-> %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    stopwatch.reset();

    System.out.println("Creating tables.");
    loadStatements("osm_create_tables.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
    System.out.println(String.format("-> %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    stopwatch.reset();

    System.out.println("Creating primary keys.");
    loadStatements("osm_create_primary_keys.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
    System.out.println(String.format("-> %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    stopwatch.reset();

    stopwatch.start();
    Path lmdbPath = Files.createTempDirectory("gazetteer_");
    Env<ByteBuffer> env = Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(lmdbPath.toFile());
    Store<Long, Coordinate> coordinateStore = new LmdbCoordinateCache(env, env.openDbi("coordinates", MDB_CREATE));
    Store<Long, List<Long>> referenceStore = new LmdbReferenceCache(env, env.openDbi("references", MDB_CREATE));

    System.out.println("Populating cache.");
    try (InputStream input = input(url(source))) {
      Stream<FileBlock> blocks = StreamSupport.stream(new FileBlockSpliterator(new DataInputStream(input)), false);
      CacheConsumer blockConsumer = new CacheConsumer(coordinateStore, referenceStore);
      blocks.forEach(blockConsumer);
    }
    System.out.println(String.format("-> %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    stopwatch.reset();

    stopwatch.start();
    System.out.println("Populating database.");
    try (InputStream input = input(url(source))) {
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
      System.out.println(String.format("-> %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
      stopwatch.reset();
    }

    stopwatch.start();
    System.out.println("Indexing geometries.");
    loadStatements("osm_create_gist_indexes.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
    System.out.println(String.format("-> %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    stopwatch.reset();

    stopwatch.start();
    System.out.println("Indexing attributes.");
    loadStatements("osm_create_gin_indexes.sql").forEach(statement -> {
      try (Connection connection = datasource.getConnection()) {
        connection.createStatement().execute(statement);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
    System.out.println(String.format("-> %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    stopwatch.reset();

    return 0;
  }

  public Stream<String> loadStatements(String path) throws IOException {
    URL url = Resources.getResource(path);
    String sql = Resources.toString(url, Charsets.UTF_8);
    return Stream.of(sql.split(";")).map(statement -> statement.trim()).parallel();
  }

}
