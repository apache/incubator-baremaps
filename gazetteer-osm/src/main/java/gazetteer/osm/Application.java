package gazetteer.osm;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.cache.EntityCache;
import gazetteer.osm.cache.NodeConsumer;
import gazetteer.osm.cache.NodeEntityType;
import gazetteer.osm.osmpbf.PrimitiveBlockConsumer;
import gazetteer.osm.osmpbf.FileBlock;
import gazetteer.osm.model.Node;
import gazetteer.osm.osmpbf.PrimitiveBlockReader;
import gazetteer.osm.osmpbf.FileBlockReader;
import gazetteer.osm.osmpbf.FileBlockSpliterator;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.rocksdb.*;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Application {

    public static void main(String[] args) throws RocksDBException {
        final String file = "/home/bchapuis/Datasets/osm/switzerland-latest.osm.pbf";
        final String database = "/home/bchapuis/Desktop/nodes.db";


        RocksDB.loadLibrary();

        final Options options = new Options()
                .setCreateIfMissing(true)
                .setCompressionType(CompressionType.NO_COMPRESSION);

        //JniDBFactory.pushMemoryPool(1024 * 1024 * 1024);

        try (final RocksDB db = RocksDB.open(options, database)) {
            //String file = "/home/bchapuis/Projects/resources/osm/planet-latest.osm.pbf";
            // read the OSMHeader block
            Osmformat.HeaderBlock osmHeader = blockStream(file)
                    .filter(Application::filterHeaderBlock)
                    .map(Application::toHeaderBlock)
                    .findFirst().get();

            EntityCache<Node> cache = new EntityCache<Node>(db, new NodeEntityType());

            writeCache(file, cache);
            db.flush(new FlushOptions().setWaitForFlush(true));

            System.out.println("node cache ready");

            writeDatabase(file, cache);

            System.out.println("Done!");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Well done!");
            //JniDBFactory.popMemoryPool();
        }
    }

    public static void writeCache(String file, EntityCache<Node> entityCache) throws ExecutionException, InterruptedException, FileNotFoundException {
        NodeConsumer consumer = new NodeConsumer(entityCache);
        Stream<List<Node>> stream = primitiveBlockStream(file).map(d -> d.getDenseNodes());
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        forkJoinPool.submit(() -> stream.forEach(consumer)).get();
    }

    public static void writeDatabase(String file, EntityCache<Node> cache) throws FileNotFoundException, ExecutionException, InterruptedException {
        PoolingDataSource dataSource = dataSource("jdbc:postgresql://localhost:5432/osm?user=osm&password=osm");
        PrimitiveBlockConsumer consumer = new PrimitiveBlockConsumer(cache, dataSource);
        Stream<PrimitiveBlockReader> stream = primitiveBlockStream(file);
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        forkJoinPool.submit(() -> stream.forEach(consumer)).get();
    }

    public static boolean filterHeaderBlock(FileBlock fileBlock) {
        return fileBlock.type.equals("OSMHeader");
    }

    public static boolean filterDataBlock(FileBlock fileBlock) {
        return fileBlock.type.equals("OSMData");
    }

    public static Osmformat.HeaderBlock toHeaderBlock(FileBlock fileBlock) {
        try {
            return Osmformat.HeaderBlock.parseFrom(fileBlock.data);
        } catch (InvalidProtocolBufferException e) {
            throw new Error("Unable to parse header block");
        }
    }

    public static Osmformat.PrimitiveBlock toPrimitiveBlock(FileBlock fileBlock) {
        try {
            return Osmformat.PrimitiveBlock.parseFrom(fileBlock.data);
        } catch (InvalidProtocolBufferException e) {
            throw new Error("Unable to parse primitive block");
        }
    }

    public static Stream<FileBlock> blockStream(String file) throws FileNotFoundException {
        DataInputStream input = new DataInputStream(new FileInputStream(new File(file)));
        FileBlockReader reader = new FileBlockReader(input);
        return StreamSupport.stream(new FileBlockSpliterator(reader), false);
    }

    public static Stream<PrimitiveBlockReader> primitiveBlockStream(String file) throws FileNotFoundException {
        return blockStream(file)
                .parallel()
                .filter(Application::filterDataBlock)
                .map(Application::toPrimitiveBlock)
                .map(PrimitiveBlockReader::new);
    }

    public static PoolingDataSource dataSource(String conn) {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(conn, null);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        return dataSource;
    }

}
