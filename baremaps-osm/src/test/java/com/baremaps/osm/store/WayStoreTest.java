/*
 * Copyright (C) 2020 The Baremaps Authors
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

package com.baremaps.osm.store;

import static com.baremaps.osm.store.DatabaseConstants.DATABASE_URL;
import static com.baremaps.osm.store.DatabaseConstants.WAY_1;
import static com.baremaps.osm.store.DatabaseConstants.WAY_2;
import static com.baremaps.osm.store.DatabaseConstants.WAY_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.osm.model.Way;
import com.baremaps.util.postgis.PostgisHelper;
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

class WayStoreTest {

  public DataSource dataSource;

  public PostgisWayStore wayStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    wayStore = new PostgisWayStore(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_drop_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void insert() {
    wayStore.put(WAY_1);
    assertEquals(WAY_1, wayStore.get(WAY_1.getId()));
  }

  @Test
  @Tag("integration")
  public void insertAll() {
    List<Way> ways = Arrays.asList(WAY_1, WAY_2, WAY_3);
    wayStore.put(ways);
    assertIterableEquals(
        ways,
        wayStore.get(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() {
    wayStore.put(WAY_1);
    wayStore.delete(WAY_1.getId());
    assertThrows(IllegalArgumentException.class, () -> wayStore.get(WAY_1.getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() {
    List<Way> ways = Arrays.asList(WAY_1, WAY_2, WAY_3);
    wayStore.put(ways);
    wayStore.delete(ways.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        wayStore.get(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void copy() {
    List<Way> ways = Arrays.asList(WAY_1, WAY_2, WAY_3);
    wayStore.copy(ways);
    assertIterableEquals(
        ways,
        wayStore.get(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}