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

package com.baremaps.importer.database;

import static com.baremaps.importer.database.DatabaseConstants.DATABASE_URL;
import static com.baremaps.importer.database.DatabaseConstants.WAY_1;
import static com.baremaps.importer.database.DatabaseConstants.WAY_2;
import static com.baremaps.importer.database.DatabaseConstants.WAY_3;
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

class WayTableTest {

  public DataSource dataSource;

  public WayTable wayTable;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    wayTable = new WayTable(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.execute(connection, "osm_create_extensions.sql");
      PostgisHelper.execute(connection, "osm_drop_tables.sql");
      PostgisHelper.execute(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  public void insert() throws DatabaseException {
    wayTable.insert(WAY_1);
    assertEquals(WAY_1, wayTable.select(WAY_1.getId()));
  }

  @Test
  @Tag("integration")
  public void insertAll() throws DatabaseException {
    List<Way> ways = Arrays.asList(WAY_1, WAY_2, WAY_3);
    wayTable.insert(ways);
    assertIterableEquals(
        ways,
        wayTable.select(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() throws DatabaseException {
    wayTable.insert(WAY_1);
    wayTable.delete(WAY_1.getId());
    assertThrows(IllegalArgumentException.class, () -> wayTable.select(WAY_1.getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() throws DatabaseException {
    List<Way> ways = Arrays.asList(WAY_1, WAY_2, WAY_3);
    wayTable.insert(ways);
    wayTable.delete(ways.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        wayTable.select(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void copy() throws DatabaseException {
    List<Way> ways = Arrays.asList(WAY_1, WAY_2, WAY_3);
    wayTable.copy(ways);
    assertIterableEquals(
        ways,
        wayTable.select(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}