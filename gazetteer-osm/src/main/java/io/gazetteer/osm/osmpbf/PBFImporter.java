package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.postgis.DatabaseUtil;
import io.gazetteer.osm.rocksdb.EntityStore;
import io.gazetteer.osm.rocksdb.EntityStoreException;
import io.gazetteer.osm.rocksdb.NodeEntityType;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static picocli.CommandLine.Option;

@Command(description = "Import OSM PBF into Postgresql")
public class PBFImporter implements Runnable {

  @Parameters(index = "0", paramLabel = "OSM_FILE", description = "The OpenStreetMap PBF file.")
  private File file;

  @Parameters(index = "1", paramLabel = "ROCKSDB_CACHE", description = "The RocksDB cache.")
  private File cache;

  @Parameters(index = "2", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @Option(
      names = {"-t", "--threads"},
      description = "The size of the thread pool.")
  private int threads = Runtime.getRuntime().availableProcessors();

  @Override
  public void run() {
    try {
      // Delete the RocksDB cache
      Path rootPath = Paths.get(cache.getPath());
      Files.walk(rootPath)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);

      try (Connection connection = DriverManager.getConnection(database)) {
        DatabaseUtil.createExtensions(connection);
        DatabaseUtil.dropTables(connection);
        DatabaseUtil.createTables(connection);
      }


      // Create the database
      try (EntityStore<Node> cache = EntityStore.open(this.cache, new NodeEntityType())) {
        ForkJoinPool executor = new ForkJoinPool(threads);

        Osmformat.HeaderBlock header =
            PBFUtil.fileBlocks(file).findFirst().map(PBFUtil::toHeaderBlock).get();

        System.out.println(header.getOsmosisReplicationBaseUrl());
        System.out.println(header.getOsmosisReplicationSequenceNumber());
        System.out.println(header.getOsmosisReplicationTimestamp());

        NodeConsumer cacheConsumer = new NodeConsumer(cache);
        Stream<List<Node>> cacheStream =
            PBFUtil.dataBlockReaders(file).map(DataBlockReader::readDenseNodes);
        executor.submit(() -> cacheStream.forEach(cacheConsumer)).get();

        PoolingDataSource pool = DatabaseUtil.createPoolingDataSource(database);
        DataBlockConsumer databaseConsumer = new DataBlockConsumer(cache, pool);
        Stream<DataBlock> databaseStream =
            PBFUtil.dataBlockReaders(file).map(DataBlockReader::read);
        executor.submit(() -> databaseStream.forEach(databaseConsumer)).get();
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (EntityStoreException e) {
      e.printStackTrace();
    } finally {
      System.out.println("Well done!");
    }
  }

  public static void main(String[] args) {
    CommandLine.run(new PBFImporter(), args);
  }
}
