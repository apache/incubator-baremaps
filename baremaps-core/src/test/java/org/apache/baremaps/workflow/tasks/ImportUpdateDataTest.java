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

import static org.apache.baremaps.testing.TestFiles.SIMPLE_DATA_DIR;
import static org.apache.baremaps.testing.TestFiles.SIMPLE_DATA_OSM_PBF;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.baremaps.database.collection.AppendOnlyBuffer;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.database.collection.IndexedDataMap;
import org.apache.baremaps.database.memory.OnHeapMemory;
import org.apache.baremaps.database.type.LongListDataType;
import org.apache.baremaps.database.type.geometry.CoordinateDataType;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.openstreetmap.postgres.PostgresCoordinateMap;
import org.apache.baremaps.openstreetmap.postgres.PostgresHeaderRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresNodeRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresReferenceMap;
import org.apache.baremaps.openstreetmap.postgres.PostgresRelationRepository;
import org.apache.baremaps.openstreetmap.postgres.PostgresRepositoryTest;
import org.apache.baremaps.openstreetmap.postgres.PostgresWayRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class ImportUpdateDataTest extends PostgresRepositoryTest {

  @Test
  @Tag("integration")
  void data() throws Exception {
    PostgresHeaderRepository headerRepository = new PostgresHeaderRepository(dataSource());
    PostgresNodeRepository nodeRepository = new PostgresNodeRepository(dataSource());
    PostgresWayRepository wayRepository = new PostgresWayRepository(dataSource());
    PostgresRelationRepository relationRepository = new PostgresRelationRepository(dataSource());

    DataMap<Long, Coordinate> coordinateMap =
        new IndexedDataMap<>(new AppendOnlyBuffer<>(new CoordinateDataType(), new OnHeapMemory()));
    DataMap<Long, List<Long>> referenceMap =
        new IndexedDataMap<>(new AppendOnlyBuffer<>(new LongListDataType(), new OnHeapMemory()));

    // Import data
    ImportOsmPbf.execute(SIMPLE_DATA_OSM_PBF, coordinateMap, referenceMap, headerRepository,
        nodeRepository, wayRepository, relationRepository, 3857);

    headerRepository.put(new Header(1l, LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0),
        "file:///" + SIMPLE_DATA_DIR, "", ""));

    // Check node importation
    assertNull(nodeRepository.get(0l));
    assertNotNull(nodeRepository.get(1l));
    assertNotNull(nodeRepository.get(2l));
    assertNotNull(nodeRepository.get(3l));
    assertNull(nodeRepository.get(4l));

    // Check way importation
    assertNull(wayRepository.get(0l));
    assertNotNull(wayRepository.get(1l));
    assertNull(wayRepository.get(2l));

    // Check relation importation
    assertNull(relationRepository.get(0l));
    assertNotNull(relationRepository.get(1l));
    assertNull(relationRepository.get(2l));

    // Check node properties
    Node node = nodeRepository.get(1l);
    Assertions.assertEquals(1, node.getLon());
    Assertions.assertEquals(1, node.getLat());

    // Check way properties
    Way way = wayRepository.get(1l);
    assertNotNull(way);

    // Update the database
    UpdateOsmDatabase.execute(new PostgresCoordinateMap(dataSource()),
        new PostgresReferenceMap(dataSource()), headerRepository, nodeRepository, wayRepository,
        relationRepository, 3857, null);

    // Check deletions
    assertNull(nodeRepository.get(0l));
    assertNull(nodeRepository.get(1l));

    // Check insertions
    assertNotNull(nodeRepository.get(2l));
    assertNotNull(nodeRepository.get(3l));
    assertNotNull(nodeRepository.get(4l));
  }
}
