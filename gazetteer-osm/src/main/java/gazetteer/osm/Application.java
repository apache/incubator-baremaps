package gazetteer.osm;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.cache.EntityCache;
import gazetteer.osm.cache.NodeConsumer;
import gazetteer.osm.cache.NodeEntityType;
import gazetteer.osm.model.FileBlock;
import gazetteer.osm.model.Node;
import gazetteer.osm.model.PrimitiveBlock;
import gazetteer.osm.database.PrimitiveBlockConsumer;
import gazetteer.osm.util.FileBlockReader;
import gazetteer.osm.util.FileBlockSpliterator;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class Application {

    public static void main(String[] args) {
        final String file = "/home/bchapuis/Datasets/osm/switzerland-latest.osm.pbf";
        final String database = "/home/bchapuis/Desktop/nodes.db";

        final Options options = new Options()
                .createIfMissing(true)
                .blockSize(16 * 1024 * 1024)
                .writeBufferSize(4 * 1024 * 1024)
                .compressionType(CompressionType.NONE);

        //JniDBFactory.pushMemoryPool(1024 * 1024 * 1024);

        try (final DB db = factory.open(new File(database), options)) {
            //String file = "/home/bchapuis/Projects/resources/osm/planet-latest.osm.pbf";
            // read the OSMHeader block
            Osmformat.HeaderBlock osmHeader = blockStream(file)
                    .filter(Application::filterHeaderBlock)
                    .map(Application::toHeaderBlock)
                    .findFirst().get();

            EntityCache<Node> cache = new EntityCache<Node>(db, new NodeEntityType());

            writeCache(file, cache);
            writeDatabase(file, cache);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            //JniDBFactory.popMemoryPool();
        }
    }

    public static void writeCache(String file, EntityCache<Node> entityCache) throws ExecutionException, InterruptedException, FileNotFoundException {
        NodeConsumer consumer = new NodeConsumer(entityCache);
        Stream<List<Node>> stream = primitiveBlockStream(file).map(d -> d.getNodes());
        ForkJoinPool forkJoinPool = new ForkJoinPool(16);
        forkJoinPool.submit(() -> stream.forEach(consumer)).get();
    }

    public static void writeDatabase(String file, EntityCache<Node> nodeStore) throws FileNotFoundException, ExecutionException, InterruptedException {
        PoolingDataSource dataSource = dataSource("jdbc:postgresql://localhost:5432/osm?user=osm&password=osm");
        PrimitiveBlockConsumer consumer = new PrimitiveBlockConsumer(nodeStore, dataSource);
        Stream<PrimitiveBlock> stream = primitiveBlockStream(file);
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

    public static Stream<PrimitiveBlock> primitiveBlockStream(String file) throws FileNotFoundException {
        return blockStream(file)
                .parallel()
                .filter(Application::filterDataBlock)
                .map(Application::toPrimitiveBlock)
                .map(PrimitiveBlock::new);
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
