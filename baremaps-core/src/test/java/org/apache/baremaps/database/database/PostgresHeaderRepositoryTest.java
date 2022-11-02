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

import static org.apache.baremaps.database.database.Constants.HEADER_0;
import static org.apache.baremaps.database.database.Constants.HEADER_1;
import static org.apache.baremaps.database.database.Constants.HEADER_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.baremaps.database.repository.PostgresHeaderRepository;
import org.apache.baremaps.database.repository.RepositoryException;
import org.apache.baremaps.openstreetmap.model.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresHeaderRepositoryTest extends DatabaseContainerTest {

  PostgresHeaderRepository headerRepository;

  @BeforeEach
  void init() throws SQLException, IOException {
    headerRepository = new PostgresHeaderRepository(dataSource());
  }

  @Test
  @Tag("integration")
  void selectAll() throws RepositoryException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerRepository.put(headers);
    assertEquals(3, headerRepository.selectAll().size());
  }

  @Test
  @Tag("integration")
  void selectLatest() throws RepositoryException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerRepository.put(headers);
    assertEquals(HEADER_2, headerRepository.selectLatest());
  }

  @Test
  @Tag("integration")
  void insert() throws RepositoryException {
    headerRepository.put(HEADER_0);
    assertEquals(HEADER_0, headerRepository.get(HEADER_0.getReplicationSequenceNumber()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws RepositoryException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerRepository.put(headers);
    assertIterableEquals(headers, headerRepository.get(
        headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws RepositoryException {
    headerRepository.put(HEADER_0);
    headerRepository.delete(HEADER_0.getReplicationSequenceNumber());
    assertNull(headerRepository.get(HEADER_0.getReplicationSequenceNumber()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws RepositoryException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerRepository.put(headers);
    headerRepository.delete(
        headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList()));
    assertIterableEquals(Arrays.asList(null, null, null), headerRepository.get(
        headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws RepositoryException {
    List<Header> headers = Arrays.asList(HEADER_0, HEADER_1, HEADER_2);
    headerRepository.copy(headers);
    assertIterableEquals(headers, headerRepository.get(
        headers.stream().map(e -> e.getReplicationSequenceNumber()).collect(Collectors.toList())));
  }
}
