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
import static com.baremaps.osm.store.DatabaseConstants.NODE_0;
import static com.baremaps.osm.store.DatabaseConstants.NODE_1;
import static com.baremaps.osm.store.DatabaseConstants.NODE_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.osm.model.Node;
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

public class NodeStoreTest {

  public DataSource dataSource;

  public PostgisNodeStore nodeStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    nodeStore = new PostgisNodeStore(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.executeScript(connection, "osm_create_extensions.sql");
      PostgisHelper.executeScript(connection, "osm_drop_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_tables.sql");
      PostgisHelper.executeScript(connection, "osm_create_primary_keys.sql");
    }
  }

  @Test
  @Tag("integration")
  public void insert() throws StoreException {
    nodeStore.put(NODE_0);
    assertEquals(NODE_0, nodeStore.get(NODE_0.getId()));
  }

  @Test
  @Tag("integration")
  public void insertAll() throws StoreException {
    List<Node> nodes = Arrays.asList(NODE_0, NODE_1, NODE_2);
    nodeStore.put(nodes);
    assertIterableEquals(nodes,
        nodeStore.get(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() throws StoreException {
    nodeStore.put(NODE_0);
    nodeStore.delete(NODE_0.getId());
    assertThrows(IllegalArgumentException.class, () -> nodeStore.get(NODE_0.getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() throws StoreException {
    List<Node> nodes = Arrays.asList(NODE_0, NODE_1, NODE_2);
    nodeStore.put(nodes);
    nodeStore.delete(nodes.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(Arrays.asList(null, null, null),
        nodeStore.get(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void copy() throws StoreException {
    List<Node> nodes = Arrays.asList(NODE_0, NODE_1, NODE_2);
    nodeStore.copy(nodes);
    assertIterableEquals(nodes,
        nodeStore.get(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}
