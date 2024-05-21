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

import static org.apache.baremaps.testing.GeometryAssertions.assertGeometryEquals;
import static org.apache.baremaps.testing.TestFiles.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataConversions;
import org.apache.baremaps.data.collection.IndexedDataMap;
import org.apache.baremaps.data.memory.OnHeapMemory;
import org.apache.baremaps.data.type.CoordinateDataType;
import org.apache.baremaps.data.type.LongListDataType;
import org.apache.baremaps.database.postgres.CoordinateMap;
import org.apache.baremaps.database.postgres.HeaderRepository;
import org.apache.baremaps.database.postgres.NodeRepository;
import org.apache.baremaps.database.postgres.PostgresRepositoryTest;
import org.apache.baremaps.database.postgres.ReferenceMap;
import org.apache.baremaps.database.postgres.RelationRepository;
import org.apache.baremaps.database.postgres.WayRepository;
import org.apache.baremaps.openstreetmap.model.Header;
import org.apache.baremaps.openstreetmap.state.StateReader;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class ImportUpdateSampleTest extends PostgresRepositoryTest {

  @Test
  @Tag("integration")
  void sample() throws Exception {
    int srid = 4326;

    // Initialize the repositories
    HeaderRepository headerRepository = new HeaderRepository(dataSource());
    NodeRepository nodeRepository = new NodeRepository(dataSource());
    WayRepository wayRepository = new WayRepository(dataSource());
    RelationRepository relationRepository = new RelationRepository(dataSource());

    // Initialize the data maps
    Map<Long, Coordinate> coordinateMap = DataConversions.asMap(
        new IndexedDataMap<>(new AppendOnlyLog<>(new CoordinateDataType(), new OnHeapMemory())));
    Map<Long, List<Long>> referenceMap = DataConversions.asMap(
        new IndexedDataMap<>(new AppendOnlyLog<>(new LongListDataType(), new OnHeapMemory())));

    // Import the sample data
    ImportOsmPbf.execute(TestFiles.SAMPLE_OSM_PBF, coordinateMap, referenceMap, headerRepository,
        nodeRepository, wayRepository, relationRepository, srid);
    assertEquals(0, headerRepository.selectLatest().getReplicationSequenceNumber());

    // Import the state file
    try (var stateInput = Files.newInputStream(TestFiles.SAMPLE_STATE_TXT)) {
      var state = new StateReader().read(stateInput);
      headerRepository.put(new Header(state.getSequenceNumber(), state.getTimestamp(),
          "file:///" + TestFiles.SAMPLE_DIR, "", ""));
      assertEquals(1, headerRepository.selectLatest().getReplicationSequenceNumber());
    }
    assertGeometryEquals(NODE_POINT_1, nodeRepository.get(1L).getGeometry(), 100);
    assertGeometryEquals(WAY_LINESTRING_4, wayRepository.get(4L).getGeometry(), 100);
    assertGeometryEquals(WAY_POLYGON_9, wayRepository.get(9L).getGeometry(), 100);
    assertGeometryEquals(RELATION_MULTIPOLYGON_20, relationRepository.get(20L).getGeometry(), 100);
    assertGeometryEquals(RELATION_MULTIPOLYGON_36, relationRepository.get(36L).getGeometry(), 100);

    // Use the database as the reference instead of the original maps
    coordinateMap = new CoordinateMap(dataSource());
    referenceMap = new ReferenceMap(dataSource());

    // Add elements to the database
    UpdateOsmDatabase.execute(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository, relationRepository, srid, null);
    assertEquals(2, headerRepository.selectLatest().getReplicationSequenceNumber());

    assertGeometryEquals(NODE_POINT_37, nodeRepository.get(37L).getGeometry(), 100);
    assertGeometryEquals(WAY_LINESTRING_40, wayRepository.get(40L).getGeometry(), 100);
    assertGeometryEquals(WAY_POLYGON_45, wayRepository.get(45L).getGeometry(), 100);
    assertGeometryEquals(RELATION_MULTIPOLYGON_56, relationRepository.get(56L).getGeometry(), 100);
    assertGeometryEquals(RELATION_MULTIPOLYGON_72, relationRepository.get(72L).getGeometry(), 100);

    // Modify elements in the database
    UpdateOsmDatabase.execute(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository, relationRepository, srid, null);
    assertEquals(3, headerRepository.selectLatest().getReplicationSequenceNumber());
    assertGeometryEquals(NODE_POINT_1_MODIFIED, nodeRepository.get(1L).getGeometry(), 100);
    assertGeometryEquals(WAY_LINESTRING_4_MODIFIED, wayRepository.get(4L).getGeometry(), 100);
    assertGeometryEquals(WAY_POLYGON_9_MODIFIED, wayRepository.get(9L).getGeometry(), 100);
    assertGeometryEquals(RELATION_MULTIPOLYGON_20_MODIFIED,
        relationRepository.get(20L).getGeometry(), 100);
    assertGeometryEquals(RELATION_MULTIPOLYGON_36_MODIFIED,
        relationRepository.get(36L).getGeometry(), 100);

    // Delete elements from the database
    UpdateOsmDatabase.execute(coordinateMap, referenceMap, headerRepository, nodeRepository,
        wayRepository, relationRepository, srid, null);
    assertEquals(4, headerRepository.selectLatest().getReplicationSequenceNumber());
    assertNull(nodeRepository.get(1L));
    assertNull(nodeRepository.get(4L));
    assertNull(nodeRepository.get(9L));
    assertNull(nodeRepository.get(20L));
    assertNull(nodeRepository.get(36L));
  }
}
