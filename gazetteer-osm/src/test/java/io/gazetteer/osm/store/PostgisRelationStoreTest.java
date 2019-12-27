package io.gazetteer.osm.store;

import static io.gazetteer.osm.TestConstants.NODE_0;
import static io.gazetteer.osm.TestConstants.NODE_1;
import static io.gazetteer.osm.TestConstants.NODE_2;
import static io.gazetteer.osm.TestConstants.NODE_BUILDER;
import static io.gazetteer.osm.TestConstants.RELATION_2;
import static io.gazetteer.osm.TestConstants.RELATION_3;
import static io.gazetteer.osm.TestConstants.RELATION_4;
import static io.gazetteer.osm.TestConstants.RELATION_BUILDER;
import static io.gazetteer.osm.TestConstants.WAY_BUILDER;
import static org.junit.jupiter.api.Assertions.*;

import io.gazetteer.osm.TestConstants;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.postgis.PostgisHelper;
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
  void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(TestConstants.DATABASE_URL);
    relationStore = new PostgisRelationStore(dataSource, RELATION_BUILDER);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  void put() {
    relationStore.put(RELATION_2.getInfo().getId(), RELATION_2);
    assertEquals(RELATION_2, relationStore.get(RELATION_2.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  void putAll() {
    List<StoreEntry<Long, Relation>> relations = Arrays.asList(
        new StoreEntry<>(RELATION_2.getInfo().getId(), RELATION_2),
        new StoreEntry<>(RELATION_3.getInfo().getId(), RELATION_3),
        new StoreEntry<>(RELATION_4.getInfo().getId(), RELATION_4));
    relationStore.putAll(relations);
    assertIterableEquals(
        relations.stream().map(e -> e.value()).collect(Collectors.toList()),
        relationStore.getAll(relations.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() {
    relationStore.put(RELATION_2.getInfo().getId(), RELATION_2);
    relationStore.delete(RELATION_2.getInfo().getId());
    assertThrows(IllegalArgumentException.class, () -> relationStore.get(RELATION_2.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() {
    List<StoreEntry<Long, Relation>> relations = Arrays.asList(
        new StoreEntry<>(RELATION_2.getInfo().getId(), RELATION_2),
        new StoreEntry<>(RELATION_3.getInfo().getId(), RELATION_3),
        new StoreEntry<>(RELATION_4.getInfo().getId(), RELATION_4));
    relationStore.putAll(relations);
    relationStore.deleteAll(relations.stream().map(e -> e.key()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        relationStore.getAll(relations.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void importAll() {
    List<StoreEntry<Long, Relation>> relations = Arrays.asList(
        new StoreEntry<>(RELATION_2.getInfo().getId(), RELATION_2),
        new StoreEntry<>(RELATION_3.getInfo().getId(), RELATION_3),
        new StoreEntry<>(RELATION_4.getInfo().getId(), RELATION_4));
    relationStore.importAll(relations);
    assertIterableEquals(
        relations.stream().map(e -> e.value()).collect(Collectors.toList()),
        relationStore.getAll(relations.stream().map(e -> e.key()).collect(Collectors.toList())));
  }
}