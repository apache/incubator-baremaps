/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;

import static org.apache.baremaps.testing.TestFiles.LIECHTENSTEIN_DIR;
import static org.apache.baremaps.testing.TestFiles.LIECHTENSTEIN_OSM_PBF;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.baremaps.database.collection.AppendOnlyBuffer;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.database.collection.IndexedDataMap;
import org.apache.baremaps.database.memory.OnHeapMemory;
import org.apache.baremaps.database.type.LongListDataType;
import org.apache.baremaps.database.type.geometry.CoordinateDataType;
import org.apache.baremaps.openstreetmap.DiffService;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.postgres.PostgresCoordinateMap;
import org.apache.baremaps.openstreetmap.postgres.PostgresHeaderRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresReferenceMap;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRepositoryTest;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class ImportUpdateLiechtensteinTest extends PostgresRepositoryTest {

  @Test
  @Tag("integration")
  void liechtenstein() throws Exception {
    PostgresHeaderRepository headerRepository = new PostgresHeaderRepository(dataSource());
    PostgresNodeRepository nodeRepository = new PostgresNodeRepository(dataSource());
    PostgresWayRepository wayRepository = new PostgresWayRepository(dataSource());
    PostgresRelationRepository relationRepository = new PostgresRelationRepository(dataSource());

    DataMap<Long, Coordinate> coordinateMap =
        new IndexedDataMap<>(new AppendOnlyBuffer<>(new CoordinateDataType(), new OnHeapMemory()));
    DataMap<Long, List<Long>> referenceMap =
        new IndexedDataMap<>(new AppendOnlyBuffer<>(new LongListDataType(), new OnHeapMemory()));

    // Import data
    ImportOsmPbf.execute(LIECHTENSTEIN_OSM_PBF, coordinateMap, referenceMap,
        headerRepository,
        nodeRepository, wayRepository, relationRepository, 3857);

    assertEquals(2434l, headerRepository.selectLatest().getReplicationSequenceNumber());

    // Fix the replicationUrl so that we can update the database with local files
    headerRepository.put(new Header(2434l, LocalDateTime.of(2019, 11, 18, 21, 19, 5, 0),
        "file:///" + LIECHTENSTEIN_DIR, "", ""));

    coordinateMap = new PostgresCoordinateMap(dataSource());
    referenceMap = new PostgresReferenceMap(dataSource());

    assertEquals(0, new DiffService(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository, relationRepository, 3857, 14).call().size());

    // Update the database
    UpdateOsmDatabase.execute(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository,
        relationRepository, 3857, null);
    assertEquals(2435l, headerRepository.selectLatest().getReplicationSequenceNumber());

    assertEquals(2, new DiffService(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository, relationRepository, 3857, 14).call().size());

    UpdateOsmDatabase.execute(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository,
        relationRepository, 3857, null);
    assertEquals(2436l, headerRepository.selectLatest().getReplicationSequenceNumber());

    assertEquals(0, new DiffService(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository, relationRepository, 3857, 14).call().size());

    UpdateOsmDatabase.execute(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository,
        relationRepository, 3857, null);
    assertEquals(2437l, headerRepository.selectLatest().getReplicationSequenceNumber());
  }
}
