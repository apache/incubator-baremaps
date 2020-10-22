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
import static com.baremaps.importer.database.DatabaseConstants.RELATION_2;
import static com.baremaps.importer.database.DatabaseConstants.RELATION_3;
import static com.baremaps.importer.database.DatabaseConstants.RELATION_4;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.osm.model.Relation;
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

class RelationTableTest {

  public DataSource dataSource;

  public RelationTable relationStore;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    relationStore = new RelationTable(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.execute(connection, "osm_create_extensions.sql");
      PostgisHelper.execute(connection, "osm_drop_tables.sql");
      PostgisHelper.execute(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  public void insert() throws DatabaseException {
    relationStore.insert(RELATION_2);
    assertEquals(RELATION_2, relationStore.select(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  public void insertAll() throws DatabaseException {
    List<Relation> relations = Arrays
        .asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.insert(relations);
    assertIterableEquals(
        relations,
        relationStore.select(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void delete() throws DatabaseException {
    relationStore.insert(RELATION_2);
    relationStore.delete(RELATION_2.getId());
    assertThrows(IllegalArgumentException.class, () -> relationStore.select(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  public void deleteAll() throws DatabaseException {
    List<Relation> relations = Arrays
        .asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.insert(relations);
    relationStore.delete(relations.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        relationStore.select(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  public void copy() throws DatabaseException {
    List<Relation> relations = Arrays
        .asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.copy(relations);
    assertIterableEquals(
        relations,
        relationStore.select(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}