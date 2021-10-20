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

class ImportUpdateMonacoTest extends PostgresBaseTest {

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
  void monaco() throws Exception {

    // Import data
    new ImportService(
            new URI("res://monaco/monaco-210801.osm.pbf"),
            blobStore,
            new MapCoordinateCache(),
            new MapReferenceCache(),
            headerTable,
            nodeTable,
            wayTable,
            relationTable,
            3857)
        .call();

    assertEquals(3047l, headerTable.selectLatest().getReplicationSequenceNumber());

    // Fix the replicationUrl so that we can update the database with local files
    headerTable.delete(3047l);
    headerTable.insert(
        new Header(
            3047l,
            LocalDateTime.of(2021, 8, 01, 20, 21, 41, 0),
            "res://monaco/monaco-updates",
            "",
            ""));

    CoordinateCache coordinateCache = new PostgresCoordinateCache(dataSource);
    ReferenceCache referenceCache = new PostgresReferenceCache(dataSource);

    // Generate the diff and update the database
    long replicationSequenceNumber = headerTable.selectLatest().getReplicationSequenceNumber();
    while (replicationSequenceNumber < 3075) {
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
          .call();
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
      long nextReplicationSequenceNumber =
          headerTable.selectLatest().getReplicationSequenceNumber();
      assertEquals(replicationSequenceNumber + 1, nextReplicationSequenceNumber);
      replicationSequenceNumber = nextReplicationSequenceNumber;
    }
  }
}
