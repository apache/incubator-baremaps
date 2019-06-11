package io.gazetteer.osm.postgis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class NodeTableTest {

  public static final String URL = "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm";

  public Connection connection;

  public NodeTable table;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    connection = DriverManager.getConnection(URL);
    java.net.URL url = Resources.getResource("osm_create_tables.sql");
    String sql = Resources.toString(url, Charsets.UTF_8);
    connection.createStatement().execute(sql);
    table = new NodeTable();
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
              new Info(rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      table.insert(connection, insert);
      Node select = table.select(connection, insert.getInfo().getId());
      assertEquals(insert.getInfo(), select.getInfo());
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
              new Info(rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      table.insert(connection, insert);
      Node update =
          new Node(
              new Info(insert.getInfo().getId(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      table.update(connection, update);
      Node select = table.select(connection, insert.getInfo().getId());
      assertEquals(update.getInfo(), select.getInfo());
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
              new Info(rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      table.insert(connection, insert);
      table.delete(connection, insert.getInfo().getId());
      assertThrows(
          IllegalArgumentException.class, () -> table.select(connection, insert.getInfo().getId()));
    }
  }
}
