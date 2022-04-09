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

package com.baremaps.pipeline.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.collection.DataStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.collection.LongDataOpenHashMap;
import com.baremaps.collection.memory.OnHeapMemory;
import com.baremaps.collection.type.CoordinateDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Way;
import com.baremaps.pipeline.database.collection.PostgresCoordinateMap;
import com.baremaps.pipeline.database.collection.PostgresReferenceMap;
import com.baremaps.pipeline.database.repository.PostgresHeaderRepository;
import com.baremaps.pipeline.database.repository.PostgresNodeRepository;
import com.baremaps.pipeline.database.repository.PostgresRelationRepository;
import com.baremaps.pipeline.database.repository.PostgresWayRepository;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class ImportUpdateDataTest extends PostgresBaseTest {

  public BlobStore blobStore;
  public DataSource dataSource;
  public PostgresHeaderRepository headerRepository;
  public PostgresNodeRepository nodeRepository;
  public PostgresWayRepository wayRepository;
  public PostgresRelationRepository relationRepository;

  @BeforeEach
  void createRepository() throws SQLException, IOException {
    dataSource = initDataSource();
    blobStore = new ResourceBlobStore();
    headerRepository = new PostgresHeaderRepository(dataSource);
    nodeRepository = new PostgresNodeRepository(dataSource);
    wayRepository = new PostgresWayRepository(dataSource);
    relationRepository = new PostgresRelationRepository(dataSource);
  }

  @Test
  @Tag("integration")
  void data() throws Exception {
    LongDataMap<Coordinate> coordinates =
        new LongDataOpenHashMap<>(new DataStore<>(new CoordinateDataType(), new OnHeapMemory()));
    LongDataMap<List<Long>> references =
        new LongDataOpenHashMap<>(new DataStore<>(new LongListDataType(), new OnHeapMemory()));

    // Import data
    new ImportService(
            new URI("res:///simple/data.osm.pbf"),
            blobStore,
            coordinates,
            references,
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            3857)
        .call();

    headerRepository.put(
        new Header(0l, LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), "res:///simple", "", ""));

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
    new UpdateService(
            blobStore,
            new PostgresCoordinateMap(dataSource),
            new PostgresReferenceMap(dataSource),
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            3857)
        .call();

    // Check deletions
    assertNull(nodeRepository.get(0l));
    assertNull(nodeRepository.get(1l));

    // Check insertions
    assertNotNull(nodeRepository.get(2l));
    assertNotNull(nodeRepository.get(3l));
    assertNotNull(nodeRepository.get(4l));
  }
}
