package io.gazetteer.osm.rocksdb;

import com.google.common.io.Files;
import io.gazetteer.osm.domain.Info;
import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

public class EntityStoreTest {

  private EntityStore<Node> entityStore;

  @BeforeEach
  public void setUp() throws Exception {
    RocksDB db = RocksDB.open(new Options().setCreateIfMissing(true), Files.createTempDir().getPath());
    entityStore = EntityStore.open(db, "nodes", new NodeEntityType());
  }

  @AfterEach
  public void tearDown() throws Exception {
    entityStore.close();
  }

  @Test
  public void testAddGet() throws EntityStoreException {
    Random random = new Random();
    List<Long> ids = LongStream.range(0, 1000).boxed().collect(Collectors.toList());
    List<Node> nodes = ids.stream().map(id -> createNode(id, random)).collect(Collectors.toList());
    for (Node node : nodes) {
      entityStore.add(node);
      assertTrue(entityStore.get(node.getInfo().getId()).equals(node));
    }
  }

  @Test
  public void testAddAllGetAll() throws EntityStoreException {
    Random random = new Random();
    List<Long> ids = LongStream.range(0, 1000).boxed().collect(Collectors.toList());
    List<Node> nodes = ids.stream().map(id -> createNode(id, random)).collect(Collectors.toList());
    entityStore.addAll(nodes);
    List<Node> result = entityStore.getAll(ids);
    assertTrue(result.equals(nodes));
  }

  @Test
  public void delete() {
    assertThrows(EntityStoreException.class, () -> {
      Node node = createNode(1, new Random(1));
      entityStore.add(node);
      assertNotNull(entityStore.get(1));
      entityStore.delete(1);
      entityStore.get(1);
    });
  }

  @Test
  public void deleteAll() {
    assertThrows( EntityStoreException.class, () -> {
      Random random = new Random();
      List<Long> ids = LongStream.range(0, 1000).boxed().collect(Collectors.toList());
      List<Node> nodes = ids.stream().map(id -> createNode(id, random)).collect(Collectors.toList());
      entityStore.addAll(nodes);
      assertNotNull(entityStore.getAll(ids));
      entityStore.deleteAll(ids);
      entityStore.getAll(ids);
    });
  }

  private Node createNode(long id, Random random) {
    Map<String, String> tags = new HashMap<>();
    tags.put(randomString(random), randomString(random));
    User user = new User(random.nextInt(), randomString(random));
    Info info = new Info(id, random.nextInt(), random.nextInt(), random.nextInt(), user, tags);
    return new Node(info, random.nextDouble(), random.nextDouble());
  }

  private String randomString(Random random) {
    byte[] array = new byte[10];
    random.nextBytes(array);
    return new String(array);
  }
}
