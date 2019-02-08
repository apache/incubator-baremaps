package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NodeTableTest {

  public static final String URL = "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm";

  public Connection connection;

  public NodeTable table;

  @BeforeEach
  public void createTable() throws SQLException {
    connection = DriverManager.getConnection(URL);
    PostgisSchema.createTables(connection);
    table = new NodeTable();
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
      Node insert =
          new Node(
              new Info(
                  rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
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
      Node insert =
          new Node(
              new Info(
                  rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      table.insert(connection, insert);
      Node update =
          new Node(
              new Info(
                  insert.getInfo().getId(),
                  rnd.nextInt(),
                  rnd.nextInt(),
                  rnd.nextLong(),
                  rnd.nextInt(),
                  map),
              rnd.nextDouble(),
              rnd.nextDouble());
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
      Node insert =
          new Node(
              new Info(
                  rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      table.insert(connection, insert);
      table.delete(connection, insert.getInfo().getId());
      assertThrows(
          IllegalArgumentException.class, () -> table.select(connection, insert.getInfo().getId()));
    }
  }
}
