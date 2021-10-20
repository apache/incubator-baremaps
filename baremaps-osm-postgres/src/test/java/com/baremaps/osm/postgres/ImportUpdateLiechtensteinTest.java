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

package com.baremaps.osm.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.MapCoordinateCache;
import com.baremaps.osm.cache.MapReferenceCache;
import com.baremaps.osm.cache.ReferenceCache;
import com.baremaps.osm.database.DiffService;
import com.baremaps.osm.database.ImportService;
import com.baremaps.osm.database.UpdateService;
import com.baremaps.osm.domain.Header;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ImportUpdateLiechtensteinTest extends PostgresBaseTest {

  public BlobStore blobStore;
  public DataSource dataSource;
  public PostgresHeaderTable headerTable;
  public PostgresNodeTable nodeTable;
  public PostgresWayTable wayTable;
  public PostgresRelationTable relationTable;

  @BeforeEach
  void createTable() throws SQLException, IOException {
    dataSource = initDataSource();
    blobStore = new ResourceBlobStore();
    headerTable = new PostgresHeaderTable(dataSource);
    nodeTable = new PostgresNodeTable(dataSource);
    wayTable = new PostgresWayTable(dataSource);
    relationTable = new PostgresRelationTable(dataSource);
  }

  @Test
  @Tag("integration")
  void liechtenstein() throws Exception {

    // Import data
    new ImportService(
            new URI("res://liechtenstein/liechtenstein.osm.pbf"),
            blobStore,
            new MapCoordinateCache(),
            new MapReferenceCache(),
            headerTable,
            nodeTable,
            wayTable,
            relationTable,
            3857)
        .call();
    assertEquals(2434l, headerTable.selectLatest().getReplicationSequenceNumber());

    // Fix the replicationUrl so that we can update the database with local files
    headerTable.insert(
        new Header(
            2434l, LocalDateTime.of(2019, 11, 18, 21, 19, 5, 0), "res://liechtenstein", "", ""));

    CoordinateCache coordinateCache = new PostgresCoordinateCache(dataSource);
    ReferenceCache referenceCache = new PostgresReferenceCache(dataSource);

    assertEquals(
        0,
        new DiffService(
                blobStore,
                coordinateCache,
                referenceCache,
                headerTable,
                nodeTable,
                wayTable,
                relationTable,
                3857,
                14)
            .call()
            .size());

    // Update the database
    new UpdateService(
            blobStore,
            coordinateCache,
            referenceCache,
            headerTable,
            nodeTable,
            wayTable,
            relationTable,
            3857)
        .call();
    assertEquals(2435l, headerTable.selectLatest().getReplicationSequenceNumber());

    assertEquals(
        2,
        new DiffService(
                blobStore,
                coordinateCache,
                referenceCache,
                headerTable,
                nodeTable,
                wayTable,
                relationTable,
                3857,
                14)
            .call()
            .size());

    new UpdateService(
            blobStore,
            coordinateCache,
            referenceCache,
            headerTable,
            nodeTable,
            wayTable,
            relationTable,
            3857)
        .call();
    assertEquals(2436l, headerTable.selectLatest().getReplicationSequenceNumber());

    assertEquals(
        0,
        new DiffService(
                blobStore,
                coordinateCache,
                referenceCache,
                headerTable,
                nodeTable,
                wayTable,
                relationTable,
                3857,
                14)
            .call()
            .size());

    new UpdateService(
            blobStore,
            coordinateCache,
            referenceCache,
            headerTable,
            nodeTable,
            wayTable,
            relationTable,
            3857)
        .call();
    assertEquals(2437l, headerTable.selectLatest().getReplicationSequenceNumber());
  }
}
