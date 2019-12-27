package io.gazetteer.osm.store;

import static io.gazetteer.osm.OSMTestUtil.COORDINATE_TRANSFORM;
import static io.gazetteer.osm.OSMTestUtil.GEOMETRY_FACTORY;
import static io.gazetteer.osm.OSMTestUtil.NODE_0;
import static io.gazetteer.osm.OSMTestUtil.NODE_1;
import static io.gazetteer.osm.OSMTestUtil.NODE_2;
import static io.gazetteer.osm.OSMTestUtil.NODE_BUILDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.gazetteer.osm.OSMTestUtil;
import io.gazetteer.osm.geometry.NodeBuilder;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.postgis.PostgisHelper;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class PostgisNodeStoreTest {

  public DataSource dataSource;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(OSMTestUtil.DATABASE_URL);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void put() {
    PostgisNodeStore nodeStore = new PostgisNodeStore(dataSource, NODE_BUILDER);
    nodeStore.put(NODE_0.getInfo().getId(), NODE_0);
    assertEquals(NODE_0, nodeStore.get(NODE_0.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void putAll() {
    PostgisNodeStore nodeStore = new PostgisNodeStore(dataSource, NODE_BUILDER);
    List<StoreEntry<Long, Node>> nodes = Arrays.asList(
        new StoreEntry<>(NODE_0.getInfo().getId(), NODE_0),
        new StoreEntry<>(NODE_1.getInfo().getId(), NODE_1),
        new StoreEntry<>(NODE_2.getInfo().getId(), NODE_2));
    nodeStore.putAll(nodes);
    assertIterableEquals(
        nodes.stream().map(e -> e.value()).collect(Collectors.toList()),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() {
    PostgisNodeStore nodeStore = new PostgisNodeStore(dataSource, NODE_BUILDER);
    nodeStore.put(NODE_0.getInfo().getId(), NODE_0);
    nodeStore.delete(NODE_0.getInfo().getId());
    assertThrows(IllegalArgumentException.class, () -> nodeStore.get(NODE_0.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() {
    PostgisNodeStore nodeStore = new PostgisNodeStore(dataSource, NODE_BUILDER);
    List<StoreEntry<Long, Node>> nodes = Arrays.asList(
        new StoreEntry<>(NODE_0.getInfo().getId(), NODE_0),
        new StoreEntry<>(NODE_1.getInfo().getId(), NODE_1),
        new StoreEntry<>(NODE_2.getInfo().getId(), NODE_2));
    nodeStore.putAll(nodes);
    nodeStore.deleteAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList()));
    System.out.println(nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void importAll() {
    PostgisNodeStore nodeStore = new PostgisNodeStore(dataSource, NODE_BUILDER);
    List<StoreEntry<Long, Node>> nodes = Arrays.asList(
        new StoreEntry<>(NODE_0.getInfo().getId(), NODE_0),
        new StoreEntry<>(NODE_1.getInfo().getId(), NODE_1),
        new StoreEntry<>(NODE_2.getInfo().getId(), NODE_2));
    nodeStore.importAll(nodes);
    assertIterableEquals(
        nodes.stream().map(e -> e.value()).collect(Collectors.toList()),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }
}
