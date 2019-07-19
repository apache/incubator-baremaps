package io.gazetteer.cli.commands;

import static io.gazetteer.osm.osmpbf.PBFUtil.input;
import static io.gazetteer.osm.osmpbf.PBFUtil.stream;
import static io.gazetteer.osm.osmpbf.PBFUtil.url;

import io.gazetteer.cli.util.StopWatch;
import io.gazetteer.common.postgis.DatabaseUtil;
import io.gazetteer.osm.osmpbf.FileBlock;
import io.gazetteer.osm.postgis.BlockConsumer;
import java.io.InputStream;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.dbcp2.PoolingDataSource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name="import")
public class OSMImport implements Callable<Integer> {

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
    PoolingDataSource datasource = DatabaseUtil.poolingDataSource(database);
    try {
      System.out.println("Creating database.");
      try (Connection connection = datasource.getConnection()) {
        DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Populating database.");
      try (InputStream input = input(url(source))) {
        Stream<FileBlock> blocks = stream(input);
        BlockConsumer pgBulkInsertConsumer = new BlockConsumer(datasource);
        executor.submit(() -> blocks.forEach(pgBulkInsertConsumer)).get();
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = datasource.getConnection()) {
        System.out.println("Updating geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_geometries.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = datasource.getConnection()) {
        System.out.println("Creating primary keys.");
        DatabaseUtil.executeScript(connection, "osm_create_primary_keys.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = datasource.getConnection()) {
        System.out.println("Indexing geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_indexes.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = datasource.getConnection()) {
        System.out.println("Creating triggers.");
        DatabaseUtil.executeScript(connection, "osm_create_triggers.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      return 0;
    } finally {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
  }

}