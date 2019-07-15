//package io.gazetteer.osm.lmdb;
//
//import com.google.common.io.Files;
//import io.gazetteer.osm.model.DataStoreException;
//import io.gazetteer.osm.model.Info;
//import io.gazetteer.osm.model.Node;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.rocksdb.ColumnFamilyDescriptor;
//import org.rocksdb.ColumnFamilyHandle;
//import org.rocksdb.Options;
//import org.rocksdb.RocksDB;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.stream.Collectors;
//import java.util.stream.LongStream;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class EntityStoreTest {
//
//  private LmdbStore<Long, Node> entityStore;
//
//  @BeforeEach
//  public void setUp() throws Exception {
//    RocksDB db = RocksDB.open(new Options().setCreateIfMissing(true), Files.createTempDir().path());
//    ColumnFamilyHandle nodes = db.createColumnFamily(new ColumnFamilyDescriptor("nodes".getBytes()));
//    entityStore = LmdbStore.open(db, nodes, new NodeType());
//  }
//
//  @AfterEach
//  public void tearDown() throws Exception {
//    entityStore.close();
//  }
//
//  @Test
//  public void testAddGet() throws DataStoreException {
//    Random random = new Random();
//    List<Long> ids = LongStream.range(0, 1000).boxed().collect(Collectors.toList());
//    List<Node> nodes = ids.stream().map(id -> createNode(id, random)).collect(Collectors.toList());
//    for (Node node : nodes) {
//      entityStore.add(node);
//      assertTrue(entityStore.get(node.getInfo().getId()).equals(node));
//    }
//  }
//
//  @Test
//  public void testAddAllGetAll() throws DataStoreException {
//    Random random = new Random();
//    List<Long> ids = LongStream.range(0, 1000).boxed().collect(Collectors.toList());
//    List<Node> nodes = ids.stream().map(id -> createNode(id, random)).collect(Collectors.toList());
//    entityStore.addAll(nodes);
//    List<Node> result = entityStore.getAll(ids);
//    assertTrue(result.equals(nodes));
//  }
//
//  @Test
//  public void delete() {
//    assertThrows(DataStoreException.class, () -> {
//      Node node = createNode(1, new Random(1));
//      entityStore.add(node);
//      assertNotNull(entityStore.get(1l));
//      entityStore.delete(1l);
//      entityStore.get(1l);
//    });
//  }
//
//  @Test
//  public void deleteAll() {
//    assertThrows( DataStoreException.class, () -> {
//      Random random = new Random();
//      List<Long> ids = LongStream.range(0, 1000).boxed().collect(Collectors.toList());
//      List<Node> nodes = ids.stream().map(id -> createNode(id, random)).collect(Collectors.toList());
//      entityStore.addAll(nodes);
//      assertNotNull(entityStore.getAll(ids));
//      entityStore.deleteAll(ids);
//      entityStore.getAll(ids);
//    });
//  }
//
//  private Node createNode(long id, Random random) {
//    Map<String, String> tags = new HashMap<>();
//    tags.put(randomString(random), randomString(random));
//    Info info = new Info(id, random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt(), tags);
//    return new Node(info, random.nextDouble(), random.nextDouble());
//  }
//
//  private String randomString(Random random) {
//    byte[] array = new byte[10];
//    random.nextBytes(array);
//    return new String(array);
//  }
//}
