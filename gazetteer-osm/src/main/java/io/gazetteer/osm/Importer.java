package io.gazetteer.osm;

import static picocli.CommandLine.Option;

import io.gazetteer.osm.database.HeaderTable;
import io.gazetteer.osm.model.Change;
import io.gazetteer.osm.model.Header;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.DataBlockConsumer;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.osmxml.ChangeConsumer;
import io.gazetteer.osm.osmxml.ChangeUtil;
import io.gazetteer.osm.util.StopWatch;
import io.gazetteer.postgis.util.DatabaseUtil;
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
import java.util.zip.GZIPInputStream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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
  private String database;

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Override
  public void run() {
    ForkJoinPool executor = new ForkJoinPool(threads);
    try {
      StopWatch stopWatch = new StopWatch();

      System.out.println("Creating OSM database.");
      try (Connection connection = DriverManager.getConnection(database)) {
        DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Parsing OSM headers.");
      try (Connection connection = DriverManager.getConnection(database)) {
        Osmformat.HeaderBlock headerBlock = PBFUtil.fileBlocks(new FileInputStream(file)).findFirst().map(PBFUtil::toHeaderBlock).get();
        long replicationTimestamp = headerBlock.getOsmosisReplicationTimestamp();
        long replicationSequenceNumber = headerBlock.getOsmosisReplicationSequenceNumber();
        String replicationUrl = headerBlock.getOsmosisReplicationBaseUrl();
        String source = headerBlock.getSource();
        String writingProgram = headerBlock.getWritingprogram();
        HeaderBBox headerBBox = headerBlock.getBbox();
        double x1 = headerBBox.getLeft() * .000000001;
        double x2 = headerBBox.getRight() * .000000001;
        double y1 = headerBBox.getBottom() * .000000001;
        double y2 = headerBBox.getTop() * .000000001;
        GeometryFactory geometryFactory = new GeometryFactory();
        Point p1 = geometryFactory.createPoint(new Coordinate(x1, y1));
        Point p2 = geometryFactory.createPoint(new Coordinate(x2, y2));
        Geometry bbox = geometryFactory.createMultiPoint(new Point[]{p1, p2}).getEnvelope();
        Header header = new Header(replicationTimestamp, replicationSequenceNumber, replicationUrl, source, writingProgram, bbox);
        HeaderTable.insert(connection, header);
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      System.out.println("Populating OSM database.");
      PoolingDataSource pool = DatabaseUtil.poolingDataSource(database);
      DataBlockConsumer pgBulkInsertConsumer = new DataBlockConsumer(pool);
      Stream<DataBlock> postgisStream = PBFUtil.dataBlocks(new FileInputStream(file));
      executor.submit(() -> postgisStream.forEach(pgBulkInsertConsumer)).get();
      System.out.println(String.format("-> %dms", stopWatch.lap()));

      try (Connection connection = DriverManager.getConnection(database)) {
        System.out.println("Updating OSM geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_geometries.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try (Connection connection = DriverManager.getConnection(database)) {
        System.out.println("Indexing OSM geometries.");
        DatabaseUtil.executeScript(connection, "osm_create_indexes.sql");
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      }

      try {
        System.out.println("Updating OSM database");
        ChangeConsumer changeConsumer = new ChangeConsumer(pool);
        Stream<Change> changeStream = ChangeUtil.stream(
            new GZIPInputStream(new FileInputStream("/home/bchapuis/Projects/github.com/gazetteerio/gazetteer/data/liechtenstein.osc.gz")));
        executor.submit(() -> changeStream.forEach(changeConsumer)).get();
        System.out.println(String.format("-> %dms", stopWatch.lap()));
      } catch (Exception e) {
        e.printStackTrace();
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
