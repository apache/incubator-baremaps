package io.gazetteer.osm;

import io.gazetteer.osm.model.DataStoreException;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.osmpbf.PBFUtil;
import io.gazetteer.osm.postgis.PostgisConsumer;
import io.gazetteer.osm.postgis.PostgisSchema;
import io.gazetteer.osm.rocksdb.NodeType;
import io.gazetteer.osm.rocksdb.RocksdbConsumer;
import io.gazetteer.osm.rocksdb.RocksdbStore;
import io.gazetteer.osm.rocksdb.WayType;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

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
    try {
      Path rocksdbPath = Paths.get(rocksdb.getPath());

      // Delete the RocksDB rocksdb
      Files.walk(rocksdbPath)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);

      try (Connection connection = DriverManager.getConnection(postgres)) {
        PostgisSchema.createExtensions(connection);
        PostgisSchema.dropTables(connection);
        PostgisSchema.createTables(connection);
      }

      final Options options =
          new Options().setCreateIfMissing(true).setCompressionType(CompressionType.NO_COMPRESSION);

      // Create the postgres
      try (RocksDB db = RocksDB.open(options, rocksdb.getPath());
           RocksdbStore<Long, Node> nodeStore = RocksdbStore.open(db, "nodes", new NodeType());
           RocksdbStore<Long, Way> wayStore = RocksdbStore.open(db, "ways", new WayType())) {
        ForkJoinPool executor = new ForkJoinPool(threads);

        Osmformat.HeaderBlock header =
            PBFUtil.fileBlocks(file).findFirst().map(PBFUtil::toHeaderBlock).get();

        System.out.println(header.getOsmosisReplicationBaseUrl());
        System.out.println(header.getOsmosisReplicationSequenceNumber());
        System.out.println(header.getOsmosisReplicationTimestamp());

        RocksdbConsumer rocksdbConsumer = new RocksdbConsumer(nodeStore, wayStore);
        Stream<DataBlock> rocksdbStream = PBFUtil.dataBlocks(file);
        executor.submit(() -> rocksdbStream.forEach(rocksdbConsumer)).get();

        PoolingDataSource pool = PostgisSchema.createPoolingDataSource(postgres);
        PostgisConsumer postgisConsumer = new PostgisConsumer(nodeStore, pool);
        Stream<DataBlock> postgisStream = PBFUtil.dataBlocks(file);
        executor.submit(() -> postgisStream.forEach(postgisConsumer)).get();
      }
    } catch (RocksDBException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (DataStoreException e) {
      e.printStackTrace();
    } finally {
      System.out.println("Well done!");
    }
  }

  public static void main(String[] args) {
    CommandLine.run(new Importer(), args);
  }
}
