package io.gazetteer.osm.database;

import io.gazetteer.osm.OSMTestUtil;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Way;
import io.gazetteer.postgis.util.DatabaseUtil;
import java.io.IOException;

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

  public Connection connection;


  @BeforeEach
  public void createTable() throws SQLException, IOException {
    connection = DriverManager.getConnection(OSMTestUtil.DATABASE_URL);
    DatabaseUtil.executeScript(connection, "osm_create_extensions.sql");
    DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
  }

  @Test
  @Tag("integration")
  public void insert() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Way insert = new Way(new Info(rnd.nextLong(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map), Arrays.asList(1l, 2l, 3l));
      WayTable.insert(connection, insert);
      assertEquals(insert, WayTable.select(connection, insert.getInfo().getId()));
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
      WayTable.insert(connection, insert);
      Way update = new Way(new Info(insert.getInfo().getId(), rnd.nextInt(), rnd.nextInt(), rnd.nextLong(), rnd.nextInt(), map), Arrays.asList(1l, 2l, 3l));
      WayTable.update(connection, update);
      assertEquals(update, WayTable.select(connection, insert.getInfo().getId()));
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
      WayTable.insert(connection, insert);
      WayTable.delete(connection, insert.getInfo().getId());
      assertThrows(IllegalArgumentException.class, () -> WayTable.select(connection, insert.getInfo().getId()));
    }
  }
}
