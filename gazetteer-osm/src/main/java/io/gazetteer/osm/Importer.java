package io.gazetteer.osm;

import static picocli.CommandLine.Option;

import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.postgis.DatabaseUtil;
import io.gazetteer.osm.osmpbf.DataBlockConsumer;
import io.gazetteer.osm.util.StopWatch;
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
import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Envelope;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBBox;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

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
      Osmformat.HeaderBlock header = PBFUtil.fileBlocks(new FileInputStream(file)).findFirst().map(PBFUtil::toHeaderBlock).get();
      System.out.println(header.getOsmosisReplicationBaseUrl());
      System.out.println(header.getOsmosisReplicationSequenceNumber());
      System.out.println(header.getOsmosisReplicationTimestamp());

      HeaderBBox bbox = header.getBbox();
      System.out.println(header.getBbox());

      Envelope envelope = new Envelope(
          bbox.getLeft() * .000000001,
          bbox.getRight() * .000000001,
          bbox.getBottom() * .000000001,
          bbox.getTop() * .000000001);

      System.out.println(envelope);

      System.out.println(String.format("-> %dms", stopWatch.lap()));

      System.out.println("Creating OSM database.");
      try (Connection connection = DriverManager.getConnection(postgres)) {
        DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Populating OSM database.");
      PoolingDataSource pool = DatabaseUtil.createPoolingDataSource(postgres);
      DataBlockConsumer pgBulkInsertConsumer = new DataBlockConsumer(pool);
      Stream<DataBlock> postgisStream = PBFUtil.dataBlocks(new FileInputStream(file));
      executor.submit(() -> postgisStream.forEach(pgBulkInsertConsumer)).get();
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      try (Connection connection = DriverManager.getConnection(postgres)) {
        System.out.println("Updating OSM geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_geoms.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = DriverManager.getConnection(postgres)) {
        System.out.println("Indexing OSM geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_indexes.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      /*
      try {
        System.out.println("Updating OSM database");
        ChangeConsumer changeConsumer = new ChangeConsumer(pool);
        Stream<Change> changeStream = ChangeUtil.stream(new GZIPInputStream(new FileInputStream("/home/bchapuis/Projects/github.com/gazetteerio/gazetteer/gazetteer-benchmarks/src/main/resources/liechtenstein.osc.gz")));
        executor.submit(() -> changeStream.forEach(changeConsumer)).get();
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      } catch (Exception e) {
        e.printStackTrace();
      }
       */

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
