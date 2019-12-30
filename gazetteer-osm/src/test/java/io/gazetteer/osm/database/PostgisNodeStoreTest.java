package io.gazetteer.osm.database;

import static io.gazetteer.osm.TestConstants.NODE_0;
import static io.gazetteer.osm.TestConstants.NODE_1;
import static io.gazetteer.osm.TestConstants.NODE_2;
import static io.gazetteer.osm.TestConstants.NODE_BUILDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.gazetteer.osm.TestConstants;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.store.Store.Entry;
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
    dataSource = PostgisHelper.poolingDataSource(TestConstants.DATABASE_URL);
    nodeStore = new PostgisNodeStore(dataSource, NODE_BUILDER);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void put() {
    nodeStore.put(NODE_0.getInfo().getId(), NODE_0);
    assertEquals(NODE_0, nodeStore.get(NODE_0.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void putAll() {
    List<Entry<Long, Node>> nodes = Arrays.asList(
        new Entry<>(NODE_0.getInfo().getId(), NODE_0),
        new Entry<>(NODE_1.getInfo().getId(), NODE_1),
        new Entry<>(NODE_2.getInfo().getId(), NODE_2));
    nodeStore.putAll(nodes);
    assertIterableEquals(
        nodes.stream().map(e -> e.value()).collect(Collectors.toList()),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() {
    nodeStore.put(NODE_0.getInfo().getId(), NODE_0);
    nodeStore.delete(NODE_0.getInfo().getId());
    assertThrows(IllegalArgumentException.class, () -> nodeStore.get(NODE_0.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() {
    List<Entry<Long, Node>> nodes = Arrays.asList(
        new Entry<>(NODE_0.getInfo().getId(), NODE_0),
        new Entry<>(NODE_1.getInfo().getId(), NODE_1),
        new Entry<>(NODE_2.getInfo().getId(), NODE_2));
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
        new Entry<>(NODE_0.getInfo().getId(), NODE_0),
        new Entry<>(NODE_1.getInfo().getId(), NODE_1),
        new Entry<>(NODE_2.getInfo().getId(), NODE_2));
    nodeStore.importAll(nodes);
    assertIterableEquals(
        nodes.stream().map(e -> e.value()).collect(Collectors.toList()),
        nodeStore.getAll(nodes.stream().map(e -> e.key()).collect(Collectors.toList())));
  }
}
