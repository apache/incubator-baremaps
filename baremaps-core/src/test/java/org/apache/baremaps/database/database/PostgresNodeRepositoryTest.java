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
import org.apache.baremaps.database.repository.PostgresNodeRepository;
import org.apache.baremaps.database.repository.RepositoryException;
import org.apache.baremaps.openstreetmap.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresNodeRepositoryTest extends DatabaseContainerTest {

  PostgresNodeRepository nodeRepository;

  @BeforeEach
  void beforeEach() throws SQLException, IOException {
    nodeRepository = new PostgresNodeRepository(dataSource());
  }

  @Test
  @Tag("integration")
  void insert() throws RepositoryException {
    nodeRepository.put(Constants.NODE_0);
    assertEquals(Constants.NODE_0, nodeRepository.get(Constants.NODE_0.getId()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws RepositoryException {
    List<Node> nodes = Arrays.asList(Constants.NODE_0, Constants.NODE_1, Constants.NODE_2);
    nodeRepository.put(nodes);
    assertIterableEquals(nodes,
        nodeRepository.get(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws RepositoryException {
    nodeRepository.put(Constants.NODE_0);
    nodeRepository.delete(Constants.NODE_0.getId());
    assertNull(nodeRepository.get(Constants.NODE_0.getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws RepositoryException {
    List<Node> nodes = Arrays.asList(Constants.NODE_0, Constants.NODE_1, Constants.NODE_2);
    nodeRepository.put(nodes);
    nodeRepository.delete(nodes.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(Arrays.asList(null, null, null),
        nodeRepository.get(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws RepositoryException {
    List<Node> nodes = Arrays.asList(Constants.NODE_0, Constants.NODE_1, Constants.NODE_2);
    nodeRepository.copy(nodes);
    assertIterableEquals(nodes,
        nodeRepository.get(nodes.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}
