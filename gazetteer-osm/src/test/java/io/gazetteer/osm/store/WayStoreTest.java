package io.gazetteer.osm.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.gazetteer.common.postgis.DatabaseUtils;
import io.gazetteer.osm.OSMTestUtil;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.postgis.PostgisWayStore;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class WayStoreTest {

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
  public void insert() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Way way = new Way(new Info(rnd.nextLong(), rnd.nextInt(),
          LocalDateTime.ofInstant(Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
          rnd.nextLong(), rnd.nextInt(), map), Arrays.asList(1l, 2l, 3l));
      PostgisWayStore wayMapper = new PostgisWayStore(dataSource, null);
      wayMapper.put(way.getInfo().getId(), way);
      assertEquals(way, wayMapper.get(way.getInfo().getId()));
    }
  }

  @Test
  @Tag("integration")
  public void delete() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Way way = new Way(new Info(rnd.nextLong(), rnd.nextInt(),
          LocalDateTime.ofInstant(Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
          rnd.nextLong(), rnd.nextInt(), map),
          Arrays.asList(1l, 2l, 3l));
      PostgisWayStore wayMapper = new PostgisWayStore(dataSource, null);
      wayMapper.put(way.getInfo().getId(), way);
      wayMapper.delete(way.getInfo().getId());
      assertThrows(IllegalArgumentException.class, () -> wayMapper.get(way.getInfo().getId()));
    }
  }
}
