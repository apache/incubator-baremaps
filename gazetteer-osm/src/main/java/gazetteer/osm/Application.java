package gazetteer.osm;

import gazetteer.osm.rocksdb.EntityCache;
import gazetteer.osm.rocksdb.EntityCacheException;
import gazetteer.osm.rocksdb.NodeConsumer;
import gazetteer.osm.rocksdb.NodeEntityType;
import gazetteer.osm.osmpbf.*;
import gazetteer.osm.domain.Node;
import gazetteer.osm.postgis.DataSources;
import org.apache.commons.dbcp2.*;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import java.io.File;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;


public class Application implements Runnable {

    @Parameters(index = "0", paramLabel = "OSM_FILE", description="The OpenStreetMap PBF file.")
    private File file;

    @Parameters(index = "1", paramLabel = "ROCKSDB_DIRECTORY", description="The Rocksdb directory.")
    private File database;

    @Override
    public void run() {
        try (final EntityCache<Node> cache = EntityCache.open(database, new NodeEntityType())) {
            Osmformat.HeaderBlock osmHeader = FileBlocks.stream(file)
                    .filter(FileBlocks::isHeaderBlock)
                    .map(FileBlocks::toHeaderBlock)
                    .findFirst().get();
            ForkJoinPool pool = new ForkJoinPool(8);

            NodeConsumer cacheConsumer = new NodeConsumer(cache);
            Stream<List<Node>> cacheStream = PrimitiveBlocks.stream(file).map(PrimitiveBlockReader::readDenseNodes);
            pool.submit(() -> cacheStream.forEach(cacheConsumer)).get();

            PoolingDataSource dataSource = DataSources.create("jdbc:postgresql://localhost:5432/osm?user=osm&password=osm");
            PrimitiveBlockConsumer databaseConsumer = new PrimitiveBlockConsumer(cache, dataSource);
            Stream<PrimitiveBlock> databaseStream = PrimitiveBlocks.stream(file).map(PrimitiveBlockReader::read);
            pool.submit(() -> databaseStream.forEach(databaseConsumer)).get();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (EntityCacheException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Well done!");
        }
    }


    public static void main(String[] args)  {
        final String file = "/home/bchapuis/Datasets/osm/switzerland-latest.osm.pbf";
        final String database = "/home/bchapuis/Desktop/nodes.db";


        CommandLine.run(new Application(), file, database);
    }



}
