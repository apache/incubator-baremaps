package io.gazetteer.osm.database;

import static io.gazetteer.osm.TestUtils.RELATION_2;
import static io.gazetteer.osm.TestUtils.RELATION_3;
import static io.gazetteer.osm.TestUtils.RELATION_4;
import static io.gazetteer.osm.TestUtils.RELATION_BUILDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.gazetteer.osm.TestUtils;
import io.gazetteer.osm.model.Relation;
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

class PostgisRelationStoreTest {


  public DataSource dataSource;

  public PostgisRelationStore relationStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(TestUtils.DATABASE_URL);
    relationStore = new PostgisRelationStore(dataSource, RELATION_BUILDER);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void put() {
    relationStore.put(RELATION_2.getInfo().getId(), RELATION_2);
    assertEquals(RELATION_2, relationStore.get(RELATION_2.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void putAll() {
    List<Entry<Long, Relation>> relations = Arrays.asList(
        new Entry<>(RELATION_2.getInfo().getId(), RELATION_2),
        new Entry<>(RELATION_3.getInfo().getId(), RELATION_3),
        new Entry<>(RELATION_4.getInfo().getId(), RELATION_4));
    relationStore.putAll(relations);
    assertIterableEquals(
        relations.stream().map(e -> e.value()).collect(Collectors.toList()),
        relationStore.getAll(relations.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() {
    relationStore.put(RELATION_2.getInfo().getId(), RELATION_2);
    relationStore.delete(RELATION_2.getInfo().getId());
    assertThrows(IllegalArgumentException.class, () -> relationStore.get(RELATION_2.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() {
    List<Entry<Long, Relation>> relations = Arrays.asList(
        new Entry<>(RELATION_2.getInfo().getId(), RELATION_2),
        new Entry<>(RELATION_3.getInfo().getId(), RELATION_3),
        new Entry<>(RELATION_4.getInfo().getId(), RELATION_4));
    relationStore.putAll(relations);
    relationStore.deleteAll(relations.stream().map(e -> e.key()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        relationStore.getAll(relations.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void importAll() {
    List<Entry<Long, Relation>> relations = Arrays.asList(
        new Entry<>(RELATION_2.getInfo().getId(), RELATION_2),
        new Entry<>(RELATION_3.getInfo().getId(), RELATION_3),
        new Entry<>(RELATION_4.getInfo().getId(), RELATION_4));
    relationStore.importAll(relations);
    assertIterableEquals(
        relations.stream().map(e -> e.value()).collect(Collectors.toList()),
        relationStore.getAll(relations.stream().map(e -> e.key()).collect(Collectors.toList())));
  }
}