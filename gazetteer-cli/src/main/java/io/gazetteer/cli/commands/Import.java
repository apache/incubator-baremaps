package io.gazetteer.cli.commands;

import static io.gazetteer.osm.osmpbf.PBFUtil.input;
import static io.gazetteer.osm.osmpbf.PBFUtil.stream;
import static io.gazetteer.osm.osmpbf.PBFUtil.url;
import static org.lmdbjava.DbiFlags.MDB_CREATE;

import io.gazetteer.cli.util.StopWatch;
import io.gazetteer.common.postgis.DatabaseUtils;
import io.gazetteer.osm.lmdb.LmdbConsumer;
import io.gazetteer.osm.lmdb.LmdbCoordinateStore;
import io.gazetteer.osm.lmdb.LmdbReferenceStore;
import io.gazetteer.osm.geometry.NodeGeometryBuilder;
import io.gazetteer.osm.geometry.RelationGeometryBuilder;
import io.gazetteer.osm.geometry.WayGeometryBuilder;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.osmpbf.CopyConsumer;
import io.gazetteer.osm.osmpbf.FileBlock;
import io.gazetteer.osm.postgis.PostgisHeaderStore;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import io.gazetteer.osm.postgis.PostgisRelationStore;
import io.gazetteer.osm.postgis.PostgisWayStore;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "import")
public class Import implements Callable<Integer> {

  @Parameters(index = "0", paramLabel = "PBF_URL", description = "The PBF file.")
  private String source;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Override
  public Integer call() throws Exception {
    StopWatch stopWatch = new StopWatch();
    ForkJoinPool executor = new ForkJoinPool(threads);
    PoolingDataSource datasource = DatabaseUtils.poolingDataSource(database);
    try {
      System.out.println("Creating database.");
      try (Connection connection = datasource.getConnection()) {
        DatabaseUtils.executeScript(connection, "osm_create_tables.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = datasource.getConnection()) {
        System.out.println("Creating primary keys.");
        DatabaseUtils.executeScript(connection, "osm_create_primary_keys.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      Path lmdbPath = Paths.get("/tmp/lmdb");
      Env<ByteBuffer> env = Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(lmdbPath.toFile());
      Store<Long, Coordinate> coordinateStore = new LmdbCoordinateStore(env, env.openDbi("coordinates", MDB_CREATE));
      Store<Long, List<Long>> referenceStore = new LmdbReferenceStore(env, env.openDbi("references", MDB_CREATE));
      System.out.println("Populating cache.");
      try (InputStream input = input(url(source))) {
        Stream<FileBlock> blocks = stream(input);
        LmdbConsumer blockConsumer = new LmdbConsumer(coordinateStore, referenceStore);
        executor.submit(() -> blocks.forEach(blockConsumer)).get();
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Populating database.");
      try (InputStream input = input(url(source))) {
        Stream<FileBlock> blocks = stream(input);
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
        PostgisHeaderStore headerMapper = new PostgisHeaderStore(datasource);
        PostgisNodeStore nodeMapper = new PostgisNodeStore(datasource, new NodeGeometryBuilder(geometryFactory));
        PostgisWayStore wayMapper = new PostgisWayStore(datasource, new WayGeometryBuilder(geometryFactory, coordinateStore));
        PostgisRelationStore relationMapper = new PostgisRelationStore(datasource, new RelationGeometryBuilder(geometryFactory, coordinateStore, referenceStore));
        CopyConsumer blockConsumer = new CopyConsumer(headerMapper, nodeMapper, wayMapper, relationMapper);
        executor.submit(() -> blocks.forEach(blockConsumer)).get();
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = datasource.getConnection()) {
        System.out.println("Indexing geometries.");
        DatabaseUtils.executeScript(connection, "osm_create_indexes.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      return 0;
    } finally {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
  }
}
