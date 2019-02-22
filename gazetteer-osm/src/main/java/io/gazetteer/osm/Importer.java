package io.gazetteer.osm;

import io.gazetteer.osm.lmdb.*;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.pgbulkinsert.PgBulkInsertConsumer;
import io.gazetteer.osm.pgbulkinsert.PgBulkInsertUtil;
import io.gazetteer.osm.postgis.PostgisSchema;
import io.gazetteer.osm.util.StopWatch;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.lmdbjava.Env;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static picocli.CommandLine.Option;

@Command(description = "Import OSM PBF into Postgresql")
public class Importer implements Runnable {

  @Parameters(index = "0", paramLabel = "OSM_FILE", description = "The OpenStreetMap PBF file.")
  private File file;

  @Parameters(index = "1", paramLabel = "LMDB_DIRECTORY", description = "The LMDB directory.")
  private File lmdb;

  @Parameters(index = "2", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String postgres;

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Override
  public void run() {
    ForkJoinPool executor = new ForkJoinPool(threads);
    try {

      StopWatch stopWatch = new StopWatch();

      System.out.println("Printing OSM headers.");
      Osmformat.HeaderBlock header =
          PBFUtil.fileBlocks(new FileInputStream(file)).findFirst().map(PBFUtil::toHeaderBlock).get();
      System.out.println(header.getOsmosisReplicationBaseUrl());
      System.out.println(header.getOsmosisReplicationSequenceNumber());
      System.out.println(header.getOsmosisReplicationTimestamp());
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      System.out.println("Cleaning LMDB cache.");
      Path lmdbPath = Paths.get(lmdb.getPath());
      if (Files.exists(lmdbPath)) Files.walk(lmdbPath).map(Path::toFile).forEach(File::delete);
      lmdbPath.toFile().mkdirs();
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      System.out.println("Populating LMDB cache.");
      LmdbConsumer lmdbConsumer = LmdbUtil.consumer(lmdbPath);
      Stream<DataBlock> lmdbStream = PBFUtil.dataBlocks(new FileInputStream(file));
      executor.submit(() -> lmdbStream.forEach(lmdbConsumer)).get();
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      System.out.println("Creating postgis database.");
      try (Connection connection = DriverManager.getConnection(postgres)) {
        PostgisSchema.createExtensions(connection);
        PostgisSchema.dropIndices(connection);
        PostgisSchema.dropTables(connection);
        PostgisSchema.createTables(connection);
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Populating postgis database.");
      PgBulkInsertConsumer pgBulkInsertConsumer = PgBulkInsertUtil.consumer(postgres);
      Stream<DataBlock> postgisStream = PBFUtil.dataBlocks(new FileInputStream(file));
      executor.submit(() -> postgisStream.forEach(pgBulkInsertConsumer)).get();
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      System.out.println("Optimizing postgis cache.");
      try (Connection connection = DriverManager.getConnection(postgres)) {
        PostgisSchema.createIndices(connection);
      }
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      System.out.println("Done!");

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      executor.shutdown();
      try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    CommandLine.run(new Importer(), args);
  }
}
