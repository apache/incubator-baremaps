/*
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

package org.apache.baremaps.database.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.baremaps.database.repository.PostgresRelationRepository;
import org.apache.baremaps.database.repository.RepositoryException;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresRelationRepositoryTest extends DatabaseContainerTest {

  PostgresRelationRepository relationRepository;

  @BeforeEach
  void init() throws SQLException, IOException {
    relationRepository = new PostgresRelationRepository(dataSource());
  }

  @Test
  @Tag("integration")
  void insert() throws RepositoryException {
    relationRepository.put(Constants.RELATION_2);
    assertEquals(Constants.RELATION_2, relationRepository.get(Constants.RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws RepositoryException {
    List<Relation> relations =
        Arrays.asList(Constants.RELATION_2, Constants.RELATION_3, Constants.RELATION_4);
    relationRepository.put(relations);
    assertIterableEquals(relations, relationRepository
        .get(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws RepositoryException {
    relationRepository.put(Constants.RELATION_2);
    relationRepository.delete(Constants.RELATION_2.getId());
    assertNull(relationRepository.get(Constants.RELATION_2.getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws RepositoryException {
    List<Relation> relations =
        Arrays.asList(Constants.RELATION_2, Constants.RELATION_3, Constants.RELATION_4);
    relationRepository.put(relations);
    relationRepository.delete(relations.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(Arrays.asList(null, null, null), relationRepository
        .get(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws RepositoryException {
    List<Relation> relations =
        Arrays.asList(Constants.RELATION_2, Constants.RELATION_3, Constants.RELATION_4);
    relationRepository.copy(relations);
    assertIterableEquals(relations, relationRepository
        .get(relations.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}
