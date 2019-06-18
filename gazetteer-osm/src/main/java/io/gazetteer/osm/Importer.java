package io.gazetteer.osm;

import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.postgis.EntityConsumer;
import io.gazetteer.osm.postgis.DatabaseUtil;
import io.gazetteer.osm.util.StopWatch;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static picocli.CommandLine.Option;

@Command(description = "Import OSM PBF into Postgresql")
public class Importer implements Runnable {

  @Parameters(index = "0", paramLabel = "OSM_FILE", description = "The OpenStreetMap PBF file.")
  private File file;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
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

      System.out.println("Creating postgis database.");
      try (Connection connection = DriverManager.getConnection(postgres)) {
        DatabaseUtil.executeScript(connection, "osm_create_extensions.sql");
        DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Populating postgis database.");
      PoolingDataSource pool = DatabaseUtil.createPoolingDataSource(postgres);
      EntityConsumer pgBulkInsertConsumer = new EntityConsumer(pool);
      Stream<DataBlock> postgisStream = PBFUtil.dataBlocks(new FileInputStream(file));
      executor.submit(() -> postgisStream.forEach(pgBulkInsertConsumer)).get();
      System.out.println(String.format("-> %dms", stopWatch.lap()));


      try (Connection connection = DriverManager.getConnection(postgres)) {
        System.out.println("Creating postgis geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_geoms.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = DriverManager.getConnection(postgres)) {
        System.out.println("Creating postgis geometries.");
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
