package io.gazetteer.osm;

import static picocli.CommandLine.Option;

import io.gazetteer.common.postgis.util.DatabaseUtil;
import io.gazetteer.osm.osmpbf.FileBlock;
import io.gazetteer.osm.osmpbf.PbfUtil;
import io.gazetteer.osm.postgis.BlockConsumer;
import io.gazetteer.osm.util.StopWatch;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(description = "Import OSM PBF into Postgresql")
public class Importer implements Runnable {

  @Parameters(index = "0", paramLabel = "OSM_FILE", description = "The OpenStreetMap PBF sourceURL.")
  private String source;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  public URL sourceURL(String source) throws MalformedURLException {
    if (Files.exists(Paths.get(source))) {
      return Paths.get(source).toUri().toURL();
    } else {
      return new URL(source);
    }
  }

  @Override
  public void run() {
    ForkJoinPool executor = new ForkJoinPool(threads);
    try {
      StopWatch stopWatch = new StopWatch();
      PoolingDataSource pool = DatabaseUtil.poolingDataSource(database);

      System.out.println("Creating OSM database.");
      try (Connection connection = pool.getConnection()) {
        DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Populating OSM database.");
      Stream<FileBlock> blocks = PbfUtil.stream(PbfUtil.read(sourceURL(source)));
      BlockConsumer pgBulkInsertConsumer = new BlockConsumer(pool);
      executor.submit(() -> blocks.forEach(pgBulkInsertConsumer)).get();
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      try (Connection connection = pool.getConnection()) {
        System.out.println("Updating OSM geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_geometries.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = pool.getConnection()) {
        System.out.println("Indexing OSM geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_indexes.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

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
