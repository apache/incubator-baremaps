package io.gazetteer.osm.osmpbf;

import static io.gazetteer.osm.osmpbf.PBFUtil.url;
import static io.gazetteer.osm.osmpbf.PBFUtil.input;
import static io.gazetteer.osm.osmpbf.PBFUtil.stream;

import io.gazetteer.common.postgis.util.DatabaseUtil;
import io.gazetteer.osm.postgis.BlockConsumer;
import io.gazetteer.osm.util.StopWatch;
import java.io.InputStream;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import javax.sql.DataSource;

public class PBFImporter implements Callable<Integer> {

  private final String source;

  private final DataSource database;

  private final ExecutorService executor;

  public PBFImporter(String source, DataSource database, ExecutorService executor) {
    this.source = source;
    this.database = database;
    this.executor = executor;
  }

  @Override
  public Integer call() throws Exception {
    StopWatch stopWatch = new StopWatch();

    System.out.println("Creating database.");
    try (Connection connection = database.getConnection()) {
      DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
      System.out.println(String.format("-> %dms", stopWatch.lap()));
    }

    System.out.println("Populating database.");
    try (InputStream input = input(url(source))) {
      Stream<FileBlock> blocks = stream(input);
      BlockConsumer pgBulkInsertConsumer = new BlockConsumer(database);
      executor.submit(() -> blocks.forEach(pgBulkInsertConsumer)).get();
      System.out.println(String.format("-> %dms", stopWatch.lap()));
    }

    try (Connection connection = database.getConnection()) {
      System.out.println("Updating geometries.");
      DatabaseUtil.executeScript(connection, "osm_create_geometries.sql");
      System.out.println(String.format("-> %dms", stopWatch.lap()));
    }

    try (Connection connection = database.getConnection()) {
      System.out.println("Creating primary keys.");
      DatabaseUtil.executeScript(connection, "osm_create_primary_keys.sql");
      System.out.println(String.format("-> %dms", stopWatch.lap()));
    }

    try (Connection connection = database.getConnection()) {
      System.out.println("Indexing geometries.");
      DatabaseUtil.executeScript(connection, "osm_create_indexes.sql");
      System.out.println(String.format("-> %dms", stopWatch.lap()));
    }

    try (Connection connection = database.getConnection()) {
      System.out.println("Creating triggers.");
      DatabaseUtil.executeScript(connection, "osm_create_triggers.sql");
      System.out.println(String.format("-> %dms", stopWatch.lap()));
    }

    return 1;
  }

}
