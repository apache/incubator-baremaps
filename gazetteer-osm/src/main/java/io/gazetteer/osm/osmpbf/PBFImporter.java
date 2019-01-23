package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.postgis.DatabaseUtil;
import io.gazetteer.osm.rocksdb.EntityStore;
import io.gazetteer.osm.rocksdb.EntityStoreException;
import io.gazetteer.osm.rocksdb.NodeConsumer;
import io.gazetteer.osm.rocksdb.NodeEntityType;
import org.apache.commons.dbcp2.PoolingDataSource;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static picocli.CommandLine.Option;


@Command(description = "Import OSM into Postgresql")
public class PBFImporter implements Runnable {

    @Parameters(index = "0", paramLabel = "OSM_FILE", description = "The OpenStreetMap PBF file.")
    private File file;

    @Parameters(index = "1", paramLabel = "ROCKSDB_CACHE", description = "The RocksDB cache.")
    private File cache;

    @Parameters(index = "2", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
    private String database;

    @Option(names = {"-t", "--threads"}, description = "The size of the thread pool.")
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

            // Reset the database
            try (Connection connection = DriverManager.getConnection(database)) {
                connection.prepareStatement(DatabaseUtil.DROP_TABLE_NODES).execute();
                connection.prepareStatement(DatabaseUtil.DROP_TABLE_WAYS).execute();
                connection.prepareStatement(DatabaseUtil.DROP_TABLE_RELATIONS).execute();
                connection.prepareStatement(DatabaseUtil.CREATE_TABLE_NODES).execute();
                connection.prepareStatement(DatabaseUtil.CREATE_TABLE_WAYS).execute();
                connection.prepareStatement(DatabaseUtil.CREATE_TABLE_RELATIONS).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create the database
            try (final EntityStore<Node> cache = EntityStore.open(this.cache, new NodeEntityType())) {
                ForkJoinPool executor = new ForkJoinPool(threads);

                NodeConsumer cacheConsumer = new NodeConsumer(cache);
                Stream<List<Node>> cacheStream = PrimitiveBlockUtil
                        .stream(file)
                        .map(PrimitiveBlockReader::readDenseNodes);
                executor.submit(() -> cacheStream.forEach(cacheConsumer)).get();

                PoolingDataSource pool = DatabaseUtil.create(database);
                PrimitiveBlockConsumer databaseConsumer = new PrimitiveBlockConsumer(cache, pool);
                Stream<PrimitiveBlock> databaseStream = PrimitiveBlockUtil
                        .stream(file)
                        .map(PrimitiveBlockReader::read);
                executor.submit(() -> databaseStream.forEach(databaseConsumer)).get();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
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
