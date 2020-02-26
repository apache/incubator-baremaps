package com.baremaps.osm.postgis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.osm.TestUtils;
import com.baremaps.osm.store.Store.Entry;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.osm.model.Node;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class PostgisNodeStoreTest {

  public DataSource dataSource;

  public PostgisNodeStore nodeStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(TestUtils.DATABASE_URL);
    nodeStore = new PostgisNodeStore(dataSource, TestUtils.NODE_BUILDER);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_drop_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void put() {
    nodeStore.put(TestUtils.NODE_0.getInfo().getId(), TestUtils.NODE_0);
    assertEquals(TestUtils.NODE_0, nodeStore.get(TestUtils.NODE_0.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void putAll() {
    List<Entry<Long, Node>> nodes = Arrays.asList(
        new Entry<>(TestUtils.NODE_0.getInfo().getId(), TestUtils.NODE_0),
        new Entry<>(TestUtils.NODE_1.getInfo().getId(), TestUtils.NODE_1),
        new Entry<>(TestUtils.NODE_2.getInfo().getId(), TestUtils.NODE_2));
    nodeStore.putAll(nodes);
    assertIterableEquals(
        nodes.stream().map(e -> e.value()).collect(Collectors.toList()),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() {
    nodeStore.put(TestUtils.NODE_0.getInfo().getId(), TestUtils.NODE_0);
    nodeStore.delete(TestUtils.NODE_0.getInfo().getId());
    assertThrows(IllegalArgumentException.class, () -> nodeStore.get(TestUtils.NODE_0.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() {
    List<Entry<Long, Node>> nodes = Arrays.asList(
        new Entry<>(TestUtils.NODE_0.getInfo().getId(), TestUtils.NODE_0),
        new Entry<>(TestUtils.NODE_1.getInfo().getId(), TestUtils.NODE_1),
        new Entry<>(TestUtils.NODE_2.getInfo().getId(), TestUtils.NODE_2));
    nodeStore.putAll(nodes);
    nodeStore.deleteAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void importAll() {
    List<Entry<Long, Node>> nodes = Arrays.asList(
        new Entry<>(TestUtils.NODE_0.getInfo().getId(), TestUtils.NODE_0),
        new Entry<>(TestUtils.NODE_1.getInfo().getId(), TestUtils.NODE_1),
        new Entry<>(TestUtils.NODE_2.getInfo().getId(), TestUtils.NODE_2));
    nodeStore.importAll(nodes);
    assertIterableEquals(
        nodes.stream().map(e -> e.value()).collect(Collectors.toList()),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }
}
