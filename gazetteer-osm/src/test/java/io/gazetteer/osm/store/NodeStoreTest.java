package io.gazetteer.osm.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.gazetteer.common.postgis.DatabaseUtils;
import io.gazetteer.osm.OSMTestUtil;
import io.gazetteer.osm.geometry.NodeGeometryBuilder;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public class NodeStoreTest {

  public DataSource dataSource;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = DatabaseUtils.poolingDataSource(OSMTestUtil.DATABASE_URL);
    try (Connection connection = dataSource.getConnection()) {
      DatabaseUtils.executeScript(connection, "osm_create_extensions.sql");
      DatabaseUtils.executeScript(connection, "osm_create_tables.sql");
      DatabaseUtils.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void put() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Node node1 = new Node(
          new Info(rnd.nextLong(), rnd.nextInt(),
              LocalDateTime.ofInstant(Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
              rnd.nextLong(), rnd.nextInt(), map),
          rnd.nextDouble(),
          rnd.nextDouble());
      GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 3857);
      NodeGeometryBuilder nodeGeometryBuilder = new NodeGeometryBuilder(geometryFactory);
      PostgisNodeStore nodeStore = new PostgisNodeStore(dataSource, nodeGeometryBuilder);
      nodeStore.put(node1.getInfo().getId(), node1);
      Node node2 = nodeStore.get(node1.getInfo().getId());
      assertEquals(node1.getInfo(), node2.getInfo());
    }
  }

  @Test
  @Tag("integration")
  public void delete() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Node node = new Node(
          new Info(rnd.nextLong(), rnd.nextInt(),
              LocalDateTime.ofInstant(Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
              rnd.nextLong(), rnd.nextInt(), map),
          rnd.nextDouble(),
          rnd.nextDouble());
      PostgisNodeStore nodeStore = new PostgisNodeStore(dataSource, null);
      nodeStore.put(node.getInfo().getId(), node);
      nodeStore.delete(node.getInfo().getId());
      assertThrows(IllegalArgumentException.class, () -> nodeStore.get(node.getInfo().getId()));
    }
  }
}
