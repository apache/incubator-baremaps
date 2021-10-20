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

import static com.baremaps.osm.postgres.Constants.RELATION_2;
import static com.baremaps.osm.postgres.Constants.RELATION_3;
import static com.baremaps.osm.postgres.Constants.RELATION_4;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.osm.database.DatabaseException;
import com.baremaps.osm.domain.Relation;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresRelationTableTest extends PostgresBaseTest {

  DataSource dataSource;

  PostgresRelationTable relationStore;

  @BeforeEach
  void createTable() throws SQLException, IOException {
    dataSource = initDataSource();
    relationStore = new PostgresRelationTable(dataSource);
  }

  @Test
  @Tag("integration")
  void insert() throws DatabaseException {
    relationStore.insert(RELATION_2);
    assertEquals(RELATION_2, relationStore.select(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws DatabaseException {
    List<Relation> relations = Arrays.asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.insert(relations);
    assertIterableEquals(
        relations,
        relationStore.select(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws DatabaseException {
    relationStore.insert(RELATION_2);
    relationStore.delete(RELATION_2.getId());
    assertNull(relationStore.select(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws DatabaseException {
    List<Relation> relations = Arrays.asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.insert(relations);
    relationStore.delete(relations.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        relationStore.select(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws DatabaseException {
    List<Relation> relations = Arrays.asList(RELATION_2, RELATION_3, RELATION_4);
    relationStore.copy(relations);
    assertIterableEquals(
        relations,
        relationStore.select(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}
