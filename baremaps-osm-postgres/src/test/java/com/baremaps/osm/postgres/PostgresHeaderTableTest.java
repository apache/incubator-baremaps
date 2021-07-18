package com.baremaps.osm.postgres;/*
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


import static com.baremaps.osm.postgres.DatabaseConstants.HEADER_0;
import static com.baremaps.osm.postgres.DatabaseConstants.HEADER_1;
import static com.baremaps.osm.postgres.DatabaseConstants.HEADER_2;
import static com.baremaps.testing.TestConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.osm.database.DatabaseException;
import com.baremaps.osm.domain.Header;
import com.baremaps.postgres.jdbc.PostgresUtils;
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

class PostgresHeaderTableTest {

  DataSource dataSource;

  PostgresHeaderTable headerStore;

  @BeforeEach
  void createTable() throws SQLException, IOException {
    dataSource = PostgresUtils.datasource(DATABASE_URL);
    headerStore = new PostgresHeaderTable(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void selectAll() throws DatabaseException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerStore.insert(headers);
    assertEquals(3, headerStore.selectAll().size());
  }

  @Test
  @Tag("integration")
  void selectLatest() throws DatabaseException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerStore.insert(headers);
    assertEquals(HEADER_2, headerStore.selectLatest());
  }

  @Test
  @Tag("integration")
  void insert() throws DatabaseException {
    headerStore.insert(HEADER_0);
    assertEquals(HEADER_0, headerStore.select(HEADER_0.getReplicationSequenceNumber()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws DatabaseException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerStore.insert(headers);
    assertIterableEquals(headers,
        headerStore.select(headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws DatabaseException {
    headerStore.insert(HEADER_0);
    headerStore.delete(HEADER_0.getReplicationSequenceNumber());
    assertNull(headerStore.select(HEADER_0.getReplicationSequenceNumber()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws DatabaseException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerStore.insert(headers);
    headerStore.delete(headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList()));
    assertIterableEquals(Arrays.asList(null, null, null),
        headerStore.select(headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws DatabaseException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerStore.copy(headers);
    assertIterableEquals(headers,
        headerStore.select(headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList())));
  }
}
