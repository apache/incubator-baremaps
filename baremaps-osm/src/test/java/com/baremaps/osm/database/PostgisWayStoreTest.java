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

package com.baremaps.osm.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.osm.TestUtils;
import com.baremaps.osm.store.Store.Entry;
import com.baremaps.core.postgis.PostgisHelper;
import com.baremaps.osm.model.Way;
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

class PostgisWayStoreTest {

  public DataSource dataSource;

  public WayTable wayStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(TestUtils.DATABASE_URL);
    wayStore = new WayTable(dataSource, TestUtils.WAY_BUILDER);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_drop_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void put() {
    wayStore.put(TestUtils.WAY_1.getInfo().getId(), TestUtils.WAY_1);
    assertEquals(TestUtils.WAY_1, wayStore.get(TestUtils.WAY_1.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void putAll() {
    List<Entry<Long, Way>> ways = Arrays.asList(
        new Entry<>(TestUtils.WAY_1.getInfo().getId(), TestUtils.WAY_1),
        new Entry<>(TestUtils.WAY_2.getInfo().getId(), TestUtils.WAY_2),
        new Entry<>(TestUtils.WAY_3.getInfo().getId(), TestUtils.WAY_3));
    wayStore.putAll(ways);
    assertIterableEquals(
        ways.stream().map(e -> e.value()).collect(Collectors.toList()),
        wayStore.getAll(ways.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() {
    wayStore.put(TestUtils.WAY_1.getInfo().getId(), TestUtils.WAY_1);
    wayStore.delete(TestUtils.WAY_1.getInfo().getId());
    assertThrows(IllegalArgumentException.class, () -> wayStore.get(TestUtils.WAY_1.getInfo().getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() {
    List<Entry<Long, Way>> ways = Arrays.asList(
        new Entry<>(TestUtils.WAY_1.getInfo().getId(), TestUtils.WAY_1),
        new Entry<>(TestUtils.WAY_2.getInfo().getId(), TestUtils.WAY_2),
        new Entry<>(TestUtils.WAY_3.getInfo().getId(), TestUtils.WAY_3));
    wayStore.putAll(ways);
    wayStore.deleteAll(ways.stream().map(e -> e.key()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        wayStore.getAll(ways.stream().map(e -> e.key()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void importAll() {
    List<Entry<Long, Way>> ways = Arrays.asList(
        new Entry<>(TestUtils.WAY_1.getInfo().getId(), TestUtils.WAY_1),
        new Entry<>(TestUtils.WAY_2.getInfo().getId(), TestUtils.WAY_2),
        new Entry<>(TestUtils.WAY_3.getInfo().getId(), TestUtils.WAY_3));
    wayStore.importAll(ways);
    assertIterableEquals(
        ways.stream().map(e -> e.value()).collect(Collectors.toList()),
        wayStore.getAll(ways.stream().map(e -> e.key()).collect(Collectors.toList())));
  }
}