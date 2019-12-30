package io.gazetteer.osm.database;

import static io.gazetteer.osm.TestConstants.DATABASE_URL;
import static io.gazetteer.osm.TestConstants.WAY_BUILDER;
import static io.gazetteer.osm.TestConstants.WAY_1;
import static io.gazetteer.osm.TestConstants.WAY_2;
import static io.gazetteer.osm.TestConstants.WAY_3;
import static org.junit.jupiter.api.Assertions.*;

import io.gazetteer.osm.model.Way;
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

class PostgisWayStoreTest {

  public DataSource dataSource;

  public PostgisWayStore wayStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    wayStore = new PostgisWayStore(dataSource, WAY_BUILDER);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  void put() {
    wayStore.put(WAY_1.getInfo().getId(), WAY_1);
    assertEquals(WAY_1, wayStore.get(WAY_1.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  void putAll() {
    List<Entry<Long, Way>> ways = Arrays.asList(
        new Entry<>(WAY_1.getInfo().getId(), WAY_1),
        new Entry<>(WAY_2.getInfo().getId(), WAY_2),
        new Entry<>(WAY_3.getInfo().getId(), WAY_3));
    wayStore.putAll(ways);
    assertIterableEquals(
        ways.stream().map(e -> e.value()).collect(Collectors.toList()),
        wayStore.getAll(ways.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() {
    wayStore.put(WAY_1.getInfo().getId(), WAY_1);
    wayStore.delete(WAY_1.getInfo().getId());
    assertThrows(IllegalArgumentException.class, () -> wayStore.get(WAY_1.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() {
    List<Entry<Long, Way>> ways = Arrays.asList(
        new Entry<>(WAY_1.getInfo().getId(), WAY_1),
        new Entry<>(WAY_2.getInfo().getId(), WAY_2),
        new Entry<>(WAY_3.getInfo().getId(), WAY_3));
    wayStore.putAll(ways);
    wayStore.deleteAll(ways.stream().map(e -> e.key()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        wayStore.getAll(ways.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void importAll() {
    List<Entry<Long, Way>> ways = Arrays.asList(
        new Entry<>(WAY_1.getInfo().getId(), WAY_1),
        new Entry<>(WAY_2.getInfo().getId(), WAY_2),
        new Entry<>(WAY_3.getInfo().getId(), WAY_3));
    wayStore.importAll(ways);
    assertIterableEquals(
        ways.stream().map(e -> e.value()).collect(Collectors.toList()),
        wayStore.getAll(ways.stream().map(e -> e.key()).collect(Collectors.toList())));
  }
}