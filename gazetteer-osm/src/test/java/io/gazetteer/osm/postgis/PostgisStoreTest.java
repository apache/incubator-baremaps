package io.gazetteer.osm.postgis;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import io.gazetteer.osm.model.DataStoreException;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.pgbulkinsert.NodeMapping;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PostgisStoreTest {

  public static final String URL = "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm";

  private PoolingDataSource pool;

  private NodeTable table;

  private PostgisStore<Long, Node> store;

  @BeforeEach
  public void createTable() throws SQLException {
    pool = PostgisSchema.createPoolingDataSource(URL);
    table = new NodeTable();
    try (Connection connection = pool.getConnection()) {
      PostgisSchema.createExtensions(connection);
      PostgisSchema.createTables(connection);
      store = new PostgisStore<>(pool, table);
    }
  }

  @AfterEach
  public void deleteTable() throws SQLException {
    try (Connection connection = pool.getConnection()) {
      PostgisSchema.dropTables(connection);
    }
  }

  @Test
  @Tag("integration")
  public void add() throws DataStoreException {
    store.add(new Node(new Info(1, 1, 1, 1, 1, new HashMap<>()), 1, 1));
    assertNotNull(store.get(1l));
  }

  @Test
  @Tag("integration")
  public void addAll() throws DataStoreException {
    store.addAll(Arrays.asList(new Node(new Info(1, 1, 1, 1, 1, new HashMap<>()), 1, 1)));
    assertNotNull(store.get(1l));
  }

  @Test
  @Tag("integration")
  public void get() {}

  @Test
  @Tag("integration")
  public void getAll() {}

  @Test
  @Tag("integration")
  public void delete() {}

  @Test
  @Tag("integration")
  public void deleteAll() {}
}
