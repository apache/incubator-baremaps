package io.gazetteer.osm.osmxml;


import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.postgis.DatabaseUtil;
import io.gazetteer.osm.rocksdb.EntityStore;
import io.gazetteer.osm.rocksdb.EntityStoreException;
import io.gazetteer.osm.rocksdb.NodeEntityType;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;

@CommandLine.Command(description = "Import OSM into Postgresql")
public class XMLImporter implements Runnable {

    @CommandLine.Parameters(index = "0", paramLabel = "OSM_FILE", description = "The OpenStreetMap PBF file.")
    private File file;

    @CommandLine.Parameters(index = "1", paramLabel = "ROCKSDB_CACHE", description = "The RocksDB cache.")
    private File cache;

    @CommandLine.Parameters(index = "2", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
    private String database;

    @CommandLine.Option(names = {"-t", "--threads"}, description = "The size of the thread pool.")
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
            }

            // Create the database
            try (EntityStore<Node> cache = EntityStore.open(this.cache, new NodeEntityType())) {
                ForkJoinPool executor = new ForkJoinPool(threads);
            }

        } catch (IOException e) {
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
        CommandLine.run(new io.gazetteer.osm.osmpbf.PBFImporter(), args);
    }

}