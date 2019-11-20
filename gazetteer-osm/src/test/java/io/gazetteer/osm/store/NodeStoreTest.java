package io.gazetteer.osm.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.gazetteer.common.postgis.DatabaseUtils;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.OSMTestUtil;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class NodeStoreTest {

  public DataSource dataSource;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = DatabaseUtils.poolingDataSource(OSMTestUtil.DATABASE_URL);
    try (Connection connection = dataSource.getConnection()) {
      DatabaseUtils.executeScript(connection, "osm_create_extensions.sql");
      DatabaseUtils.executeScript(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  public void insert() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Node node =
          new Node(
              new Info(rnd.nextLong(), rnd.nextInt(),
                  LocalDateTime.ofInstant(Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
                  rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      PostgisNodeStore nodeMapper = new PostgisNodeStore(dataSource, null);
      nodeMapper.put(node.getInfo().getId(), node);
      Node select = nodeMapper.get(node.getInfo().getId());
      assertEquals(node.getInfo(), select.getInfo());
    }
  }

  @Test
  @Tag("integration")
  public void delete() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Node node =
          new Node(
              new Info(rnd.nextLong(), rnd.nextInt(),
                  LocalDateTime.ofInstant(Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
                  rnd.nextLong(), rnd.nextInt(), map),
              rnd.nextDouble(),
              rnd.nextDouble());
      PostgisNodeStore nodeMapper = new PostgisNodeStore(dataSource, null);
      nodeMapper.put(node.getInfo().getId(), node);
      nodeMapper.delete(node.getInfo().getId());
      assertThrows(IllegalArgumentException.class, () -> nodeMapper.get(node.getInfo().getId()));
    }
  }
}
