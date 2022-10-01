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

package com.baremaps.database.database;

import static com.baremaps.testing.TestFiles.LIECHTENSTEIN_DIR;
import static com.baremaps.testing.TestFiles.LIECHTENSTEIN_OSM_PBF;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.collection.DataStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.collection.LongDataOpenHashMap;
import com.baremaps.collection.memory.OnHeapMemory;
import com.baremaps.collection.type.CoordinateDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.database.DiffService;
import com.baremaps.database.ImportService;
import com.baremaps.database.UpdateService;
import com.baremaps.database.collection.PostgresCoordinateMap;
import com.baremaps.database.collection.PostgresReferenceMap;
import com.baremaps.database.repository.PostgresHeaderRepository;
import com.baremaps.database.repository.PostgresNodeRepository;
import com.baremaps.database.repository.PostgresRelationRepository;
import com.baremaps.database.repository.PostgresWayRepository;
import com.baremaps.osm.model.Header;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class ImportUpdateLiechtensteinTest extends DatabaseContainerTest {

  @Test
  @Tag("integration")
  void liechtenstein() throws Exception {
    PostgresHeaderRepository headerRepository = new PostgresHeaderRepository(dataSource());
    PostgresNodeRepository nodeRepository = new PostgresNodeRepository(dataSource());
    PostgresWayRepository wayRepository = new PostgresWayRepository(dataSource());
    PostgresRelationRepository relationRepository = new PostgresRelationRepository(dataSource());

    LongDataMap<Coordinate> coordinates =
      new LongDataOpenHashMap<>(new DataStore<>(new CoordinateDataType(), new OnHeapMemory()));
    LongDataMap<List<Long>> references =
      new LongDataOpenHashMap<>(new DataStore<>(new LongListDataType(), new OnHeapMemory()));

    // Import data
    new ImportService(
      LIECHTENSTEIN_OSM_PBF,
      coordinates,
      references,
      headerRepository,
      nodeRepository,
      wayRepository,
      relationRepository,
      3857)
        .call();
    assertEquals(2434l, headerRepository.selectLatest().getReplicationSequenceNumber());

    // Fix the replicationUrl so that we can update the database with local files
    headerRepository.put(
      new Header(
        2434l,
        LocalDateTime.of(2019, 11, 18, 21, 19, 5, 0),
        "file:///" + LIECHTENSTEIN_DIR,
        "",
        ""));

    coordinates = new PostgresCoordinateMap(dataSource());
    references = new PostgresReferenceMap(dataSource());

    assertEquals(
      0,
      new DiffService(
        coordinates,
        references,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        3857,
        14)
          .call()
          .size());

    // Update the database
    new UpdateService(
      coordinates,
      references,
      headerRepository,
      nodeRepository,
      wayRepository,
      relationRepository,
      3857)
        .call();
    assertEquals(2435l, headerRepository.selectLatest().getReplicationSequenceNumber());

    assertEquals(
      2,
      new DiffService(
        coordinates,
        references,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        3857,
        14)
          .call()
          .size());

    new UpdateService(
      coordinates,
      references,
      headerRepository,
      nodeRepository,
      wayRepository,
      relationRepository,
      3857)
        .call();
    assertEquals(2436l, headerRepository.selectLatest().getReplicationSequenceNumber());

    assertEquals(
      0,
      new DiffService(
        coordinates,
        references,
        headerRepository,
        nodeRepository,
        wayRepository,
        relationRepository,
        3857,
        14)
          .call()
          .size());

    new UpdateService(
      coordinates,
      references,
      headerRepository,
      nodeRepository,
      wayRepository,
      relationRepository,
      3857)
        .call();
    assertEquals(2437l, headerRepository.selectLatest().getReplicationSequenceNumber());
  }
}
