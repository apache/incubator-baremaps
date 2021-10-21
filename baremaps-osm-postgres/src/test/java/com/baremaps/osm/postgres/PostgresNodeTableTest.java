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

package com.baremaps.osm.postgres;

import static com.baremaps.osm.postgres.Constants.NODE_0;
import static com.baremaps.osm.postgres.Constants.NODE_1;
import static com.baremaps.osm.postgres.Constants.NODE_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.osm.database.DatabaseException;
import com.baremaps.osm.domain.Node;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresNodeTableTest extends PostgresBaseTest {

  DataSource dataSource;

  PostgresNodeTable nodeStore;

  @BeforeEach
  void beforeEach() throws SQLException, IOException {
    dataSource = initDataSource();
    nodeStore = new PostgresNodeTable(dataSource);
  }

  @Test
  @Tag("integration")
  void insert() throws DatabaseException {
    nodeStore.insert(NODE_0);
    assertEquals(NODE_0, nodeStore.select(NODE_0.getId()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws DatabaseException {
    List<Node> nodes = Arrays.asList(NODE_0, NODE_1, NODE_2);
    nodeStore.insert(nodes);
    assertIterableEquals(
        nodes, nodeStore.select(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws DatabaseException {
    nodeStore.insert(NODE_0);
    nodeStore.delete(NODE_0.getId());
    assertNull(nodeStore.select(NODE_0.getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws DatabaseException {
    List<Node> nodes = Arrays.asList(NODE_0, NODE_1, NODE_2);
    nodeStore.insert(nodes);
    nodeStore.delete(nodes.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        nodeStore.select(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws DatabaseException {
    List<Node> nodes = Arrays.asList(NODE_0, NODE_1, NODE_2);
    nodeStore.copy(nodes);
    assertIterableEquals(
        nodes, nodeStore.select(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}
