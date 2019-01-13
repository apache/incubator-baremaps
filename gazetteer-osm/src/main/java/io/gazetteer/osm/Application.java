package io.gazetteer.osm;

import io.gazetteer.osm.osmpbf.*;
import io.gazetteer.osm.rocksdb.EntityStore;
import io.gazetteer.osm.rocksdb.EntityStoreException;
import io.gazetteer.osm.rocksdb.NodeConsumer;
import io.gazetteer.osm.rocksdb.NodeEntityType;
import gazetteer.osm.osmpbf.*;
import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.postgis.DataSources;
import org.apache.commons.dbcp2.*;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.io.File;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static picocli.CommandLine.*;


@Command(description = "Import OSM into Postgresql")
public class Application implements Runnable {


    @Parameters(index = "0", paramLabel = "OSM_FILE", description="The OpenStreetMap PBF file.")
    private File file;

    @Parameters(index = "1", paramLabel = "ROCKSDB_CACHE", description="The RocksDB cache.")
    private File database;

    @Parameters(index = "2", paramLabel = "POSTGRES_DATABASE", description="The Postgres connection.")
    private String connection;

    @Option(names = {"-t", "--threads"}, description = "MD5, SHA-1, SHA-256, ...")
    private int threads =  Runtime.getRuntime().availableProcessors();

    @Override
    public void run() {
        try (final EntityStore<Node> cache = EntityStore.open(database, new NodeEntityType())) {
            ForkJoinPool pool = new ForkJoinPool(threads);

            Osmformat.HeaderBlock osmHeader = FileBlocks.stream(file)
                    .filter(FileBlocks::isHeaderBlock)
                    .map(FileBlocks::toHeaderBlock)
                    .findFirst().get();

            NodeConsumer cacheConsumer = new NodeConsumer(cache);
            Stream<List<Node>> cacheStream = PrimitiveBlocks.stream(file).map(PrimitiveBlockReader::readDenseNodes);
            pool.submit(() -> cacheStream.forEach(cacheConsumer)).get();

            PoolingDataSource dataSource = DataSources.create(connection);
            PrimitiveBlockConsumer databaseConsumer = new PrimitiveBlockConsumer(cache, dataSource);
            Stream<PrimitiveBlock> databaseStream = PrimitiveBlocks.stream(file).map(PrimitiveBlockReader::read);
            pool.submit(() -> databaseStream.forEach(databaseConsumer)).get();

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

    public static void main(String[] args)  {
        CommandLine.run(new Application(), args);
    }



}
