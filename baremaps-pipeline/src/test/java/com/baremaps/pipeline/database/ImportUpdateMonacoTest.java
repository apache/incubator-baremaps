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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.collection.DataStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.collection.LongDataOpenHashMap;
import com.baremaps.collection.memory.OnHeapMemory;
import com.baremaps.collection.type.CoordinateDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.osm.domain.Header;
import com.baremaps.pipeline.blob.BlobStore;
import com.baremaps.pipeline.blob.ResourceBlobStore;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class ImportUpdateMonacoTest extends PostgresBaseTest {

  public BlobStore blobStore;
  public DataSource dataSource;
  public PostgresHeaderRepository headerRepository;
  public PostgresNodeRepository nodeRepository;
  public PostgresWayRepository wayRepository;
  public PostgresRelationRepository relationRepository;

  @BeforeEach
  void init() throws SQLException, IOException {
    dataSource = initDataSource();
    blobStore = new ResourceBlobStore();
    headerRepository = new PostgresHeaderRepository(dataSource);
    nodeRepository = new PostgresNodeRepository(dataSource);
    wayRepository = new PostgresWayRepository(dataSource);
    relationRepository = new PostgresRelationRepository(dataSource);
  }

  @Test
  @Tag("integration")
  void monaco() throws Exception {
    LongDataMap<Coordinate> coordinates =
        new LongDataOpenHashMap<>(new DataStore<>(new CoordinateDataType(), new OnHeapMemory()));
    LongDataMap<List<Long>> references =
        new LongDataOpenHashMap<>(new DataStore<>(new LongListDataType(), new OnHeapMemory()));

    // Import data
    new ImportService(
            new URI("res:///monaco/monaco-210801.osm.pbf"),
            blobStore,
            coordinates,
            references,
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            3857)
        .call();

    assertEquals(3047l, headerRepository.selectLatest().getReplicationSequenceNumber());

    // Fix the replicationUrl so that we can update the database with local files
    headerRepository.delete(3047l);
    headerRepository.put(
        new Header(
            3047l,
            LocalDateTime.of(2021, 8, 01, 20, 21, 41, 0),
            "res:///monaco/monaco-updates",
            "",
            ""));

    coordinates = new PostgresCoordinateMap(dataSource);
    references = new PostgresReferenceMap(dataSource);

    // Generate the diff and update the database
    long replicationSequenceNumber = headerRepository.selectLatest().getReplicationSequenceNumber();
    while (replicationSequenceNumber < 3075) {
      new DiffService(
              blobStore,
              coordinates,
              references,
              headerRepository,
              nodeRepository,
              wayRepository,
              relationRepository,
              3857,
              14)
          .call();
      new UpdateService(
              blobStore,
              coordinates,
              references,
              headerRepository,
              nodeRepository,
              wayRepository,
              relationRepository,
              3857)
          .call();
      long nextReplicationSequenceNumber =
          headerRepository.selectLatest().getReplicationSequenceNumber();
      assertEquals(replicationSequenceNumber + 1, nextReplicationSequenceNumber);
      replicationSequenceNumber = nextReplicationSequenceNumber;
    }
  }
}
