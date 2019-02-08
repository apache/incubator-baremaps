package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WayTableTest {

  public static final String URL = "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm";

  public Connection connection;

  public WayTable table;

  @BeforeEach
  public void createTable() throws SQLException {
    connection = DriverManager.getConnection(URL);
    PostgisSchema.createExtensions(connection);
    table = new WayTable(new DataStore<Long, Node>() {

      @Override
      public void add(Node object) {

      }

      @Override
      public void addAll(Collection<Node> entities) {

      }

      @Override
      public Node get(Long id) {
        return new Node(new Info(id, 1, 1, 1, 1, new HashMap<>()), 1, 1);
      }

      @Override
      public List<Node> getAll(List<Long> ids) {
        List<Node> nodes = new ArrayList<>();
        for (Long id : ids) nodes.add(get(id));
        return nodes;
      }

      @Override
      public void delete(Long id) {

      }

      @Override
      public void deleteAll(List<Long> ids) {

      }

      @Override
      public void close() {

      }
    });
    PostgisSchema.createTables(connection);
  }

  @AfterEach
  public void deleteTable() throws SQLException {
    PostgisSchema.dropTables(connection);
  }

  @Test
  @Tag("integration")
  public void insert() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Way insert = new Way(new Info(rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map), Arrays.asList(1l, 2l, 3l));
      table.insert(connection, insert);
      assertEquals(insert, table.select(connection, insert.getInfo().getId()));
    }
  }

  @Test
  @Tag("integration")
  public void update() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Way insert = new Way(new Info(rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map), Arrays.asList(1l, 2l, 3l));
      table.insert(connection, insert);
      Way update = new Way(new Info(insert.getInfo().getId(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map), Arrays.asList(1l, 2l, 3l));
      table.update(connection, update);
      assertEquals(update, table.select(connection, insert.getInfo().getId()));
    }
  }

  @Test
  @Tag("integration")
  public void delete() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Way insert = new Way(new Info(rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map), Arrays.asList(1l, 2l, 3l));
      table.insert(connection, insert);
      table.delete(connection, insert.getInfo().getId());
      assertThrows(IllegalArgumentException.class, () -> table.select(connection, insert.getInfo().getId()));
    }
  }
}
