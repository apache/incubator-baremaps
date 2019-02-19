package io.gazetteer.osm;

import io.gazetteer.osm.lmdb.LmdbStore;
import io.gazetteer.osm.lmdb.NodeType;
import io.gazetteer.osm.lmdb.WayType;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.pgbulkinsert.PgBulkInsertConsumer;
import io.gazetteer.osm.postgis.PostgisSchema;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.lmdbjava.Env;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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

  @Parameters(index = "1", paramLabel = "ROCKSDB_DIRECTORY", description = "The RocksDB directory.")
  private File rocksdb;

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

      try (Connection connection = DriverManager.getConnection(postgres)) {
        PostgisSchema.createExtensions(connection);
        PostgisSchema.dropIndices(connection);
        PostgisSchema.dropTables(connection);
        PostgisSchema.createTables(connection);
      }

      Osmformat.HeaderBlock header =
          PBFUtil.fileBlocks(file).findFirst().map(PBFUtil::toHeaderBlock).get();

      System.out.println(header.getOsmosisReplicationBaseUrl());
      System.out.println(header.getOsmosisReplicationSequenceNumber());
      System.out.println(header.getOsmosisReplicationTimestamp());

      PoolingDataSource pool = PostgisSchema.createPoolingDataSource(postgres);
      PgBulkInsertConsumer copyManagerConsumer = new PgBulkInsertConsumer(pool);
      Stream<DataBlock> postgisStream = PBFUtil.dataBlocks(file);
      executor.submit(() -> postgisStream.forEach(copyManagerConsumer)).get();

      try (Connection connection = DriverManager.getConnection(postgres)) {
        PostgisSchema.createIndices(connection);
        PostgisSchema.updateGeometryColumns(connection);
      }

      System.out.println("--------------");
      System.out.println("postgis done!");

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
