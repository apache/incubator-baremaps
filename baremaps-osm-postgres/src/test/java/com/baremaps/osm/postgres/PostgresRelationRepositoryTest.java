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

import static com.baremaps.osm.postgres.DatabaseConstants.RELATION_2;
import static com.baremaps.osm.postgres.DatabaseConstants.RELATION_3;
import static com.baremaps.osm.postgres.DatabaseConstants.RELATION_4;
import static com.baremaps.testing.TestConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.repository.RepositoryException;
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

class PostgresRelationRepositoryTest {

  DataSource dataSource;

  PostgresRelationRepository relationRepository;

  @BeforeEach
  void init() throws SQLException, IOException {
    dataSource = PostgresUtils.datasource(DATABASE_URL, 1);
    relationRepository = new PostgresRelationRepository(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void insert() throws RepositoryException {
    relationRepository.puts(RELATION_2);
    assertEquals(RELATION_2, relationRepository.get(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws RepositoryException {
    List<Relation> relations = Arrays.asList(RELATION_2, RELATION_3, RELATION_4);
    relationRepository.puts(relations);
    assertIterableEquals(
        relations,
        relationRepository.get(
            relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws RepositoryException {
    relationRepository.puts(RELATION_2);
    relationRepository.delete(RELATION_2.getId());
    assertNull(relationRepository.get(RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws RepositoryException {
    List<Relation> relations = Arrays.asList(RELATION_2, RELATION_3, RELATION_4);
    relationRepository.puts(relations);
    relationRepository.delete(relations.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        relationRepository.get(
            relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws RepositoryException {
    List<Relation> relations = Arrays.asList(RELATION_2, RELATION_3, RELATION_4);
    relationRepository.copy(relations);
    assertIterableEquals(
        relations,
        relationRepository.get(
            relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}
