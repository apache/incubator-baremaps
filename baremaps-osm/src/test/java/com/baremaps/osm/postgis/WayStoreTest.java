/*
 * Copyright (C) 2011 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.postgis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.osm.TestUtils;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.osm.model.Info;
import com.baremaps.osm.model.Way;
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
    dataSource = PostgisHelper.poolingDataSource(TestUtils.DATABASE_URL);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_drop_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void insert() throws SQLException {
    Random rnd = new Random(1);
    for (int i = 0; i < 100; i++) {
      Map<String, String> map = new HashMap<>();
      map.put("key", "val");
      Way way = new Way(
        new Info(
            rnd.nextLong(),
            rnd.nextInt(),
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
            rnd.nextLong(),
            rnd.nextInt(),
            map),
        Arrays.asList(1l, 2l, 3l));
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
      Way way = new Way(
        new Info(
            rnd.nextLong(),
            rnd.nextInt(),
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(rnd.nextInt()), TimeZone.getDefault().toZoneId()),
            rnd.nextLong(),
            rnd.nextInt(),
            map),
        Arrays.asList(1l, 2l, 3l));
      PostgisWayStore wayMapper = new PostgisWayStore(dataSource, null);
      wayMapper.put(way.getInfo().getId(), way);
      wayMapper.delete(way.getInfo().getId());
      assertThrows(IllegalArgumentException.class, () -> wayMapper.get(way.getInfo().getId()));
    }
  }
}
