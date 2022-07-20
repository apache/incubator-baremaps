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

package com.baremaps.database.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.database.repository.PostgresWayRepository;
import com.baremaps.database.repository.RepositoryException;
import com.baremaps.osm.model.Way;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class WayRepositoryTest extends DatabaseContainerTest {

  PostgresWayRepository wayRepository;

  @BeforeEach
  void init() throws SQLException, IOException {
    wayRepository = new PostgresWayRepository(dataSource());
  }

  @Test
  @Tag("integration")
  void insert() throws RepositoryException {
    wayRepository.put(Constants.WAY_1);
    assertEquals(Constants.WAY_1, wayRepository.get(Constants.WAY_1.getId()));
  }

  @Test
  @Tag("integration")
  void insertAll() throws RepositoryException {
    List<Way> ways = Arrays.asList(Constants.WAY_1, Constants.WAY_2, Constants.WAY_3);
    wayRepository.put(ways);
    assertIterableEquals(
        ways, wayRepository.get(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void delete() throws RepositoryException {
    wayRepository.put(Constants.WAY_1);
    wayRepository.delete(Constants.WAY_1.getId());
    assertNull(wayRepository.get(Constants.WAY_1.getId()));
  }

  @Test
  @Tag("integration")
  void deleteAll() throws RepositoryException {
    List<Way> ways = Arrays.asList(Constants.WAY_1, Constants.WAY_2, Constants.WAY_3);
    wayRepository.put(ways);
    wayRepository.delete(ways.stream().map(e -> e.getId()).collect(Collectors.toList()));
    assertIterableEquals(
        Arrays.asList(null, null, null),
        wayRepository.get(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }

  @Test
  @Tag("integration")
  void copy() throws RepositoryException {
    List<Way> ways = Arrays.asList(Constants.WAY_1, Constants.WAY_2, Constants.WAY_3);
    wayRepository.copy(ways);
    assertIterableEquals(
        ways, wayRepository.get(ways.stream().map(e -> e.getId()).collect(Collectors.toList())));
  }
}
