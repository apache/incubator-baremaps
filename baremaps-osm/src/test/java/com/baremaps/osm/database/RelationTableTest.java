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

import static com.baremaps.osm.database.DatabaseConstants.DATABASE_URL;
import static com.baremaps.osm.database.DatabaseConstants.RELATION_2;
import static com.baremaps.osm.database.DatabaseConstants.RELATION_3;
import static com.baremaps.osm.database.DatabaseConstants.RELATION_4;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.core.postgis.PostgisHelper;
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

class RelationTableTest {

  public DataSource dataSource;

  public RelationTable relationStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    relationStore = new RelationTable(dataSource);
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
    relationStore.put(RELATION_2);
    assertEquals(RELATION_2, relationStore.get(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  public void putAll() {
    List<RelationTable.Relation> relations = Arrays
        .asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.putAll(relations);
    assertIterableEquals(
        relations,
        relationStore.getAll(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() {
    relationStore.put(RELATION_2);
    relationStore.delete(RELATION_2.getId());
    assertThrows(IllegalArgumentException.class, () -> relationStore.get(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() {
    List<RelationTable.Relation> relations = Arrays
        .asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.putAll(relations);
    relationStore.deleteAll(relations.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        relationStore.getAll(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void importAll() {
    List<RelationTable.Relation> relations = Arrays
        .asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.importAll(relations);
    assertIterableEquals(
        relations,
        relationStore.getAll(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}