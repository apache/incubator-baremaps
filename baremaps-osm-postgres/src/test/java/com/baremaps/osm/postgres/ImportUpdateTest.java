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

import static com.baremaps.testing.TestConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.SimpleCache;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.repository.DiffService;
import com.baremaps.osm.repository.ImportService;
import com.baremaps.osm.repository.UpdateService;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class ImportUpdateTest {

  public BlobStore blobStore;
  public DataSource dataSource;
  public PostgresHeaderRepository headerRepository;
  public PostgresNodeRepository nodeRepository;
  public PostgresWayRepository wayRepository;
  public PostgresRelationRepository relationRepository;

  @BeforeEach
  void init() throws SQLException, IOException, URISyntaxException {
    dataSource = PostgresUtils.datasource(DATABASE_URL, 1);
    blobStore = new ResourceBlobStore();
    headerRepository = new PostgresHeaderRepository(dataSource);
    nodeRepository = new PostgresNodeRepository(dataSource);
    wayRepository = new PostgresWayRepository(dataSource);
    relationRepository = new PostgresRelationRepository(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void simple() throws Exception {

    // Import data
    new ImportService(
            new URI("res://simple/data.osm.pbf"),
            blobStore,
            new SimpleCache<>(),
            new SimpleCache<>(),
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            3857)
        .call();

    headerRepository.puts(
        new Header(0l, LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), "res://simple", "", ""));

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
            new PostgresCoordinateCache(dataSource),
            new PostgresReferenceCache(dataSource),
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

  @Test
  @Tag("integration")
  void liechtenstein() throws Exception {

    // Import data
    new ImportService(
            new URI("res://liechtenstein/liechtenstein.osm.pbf"),
            blobStore,
            new SimpleCache<>(),
            new SimpleCache<>(),
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            3857)
        .call();
    assertEquals(2434l, headerRepository.selectLatest().getReplicationSequenceNumber());

    // Fix the replicationUrl so that we can update the database with local files
    headerRepository.puts(
        new Header(
            2434l, LocalDateTime.of(2019, 11, 18, 21, 19, 5, 0), "res://liechtenstein", "", ""));

    Cache<Long, Coordinate> coordinateCache = new PostgresCoordinateCache(dataSource);
    Cache<Long, List<Long>> referenceCache = new PostgresReferenceCache(dataSource);

    assertEquals(
        0,
        new DiffService(
                blobStore,
                coordinateCache,
                referenceCache,
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
            blobStore,
            coordinateCache,
            referenceCache,
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            3857)
        .call();
    assertEquals(2435l, headerRepository.selectLatest().getReplicationSequenceNumber());

    assertEquals(
        7,
        new DiffService(
                blobStore,
                coordinateCache,
                referenceCache,
                headerRepository,
                nodeRepository,
                wayRepository,
                relationRepository,
                3857,
                14)
            .call()
            .size());

    new UpdateService(
            blobStore,
            coordinateCache,
            referenceCache,
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
                blobStore,
                coordinateCache,
                referenceCache,
                headerRepository,
                nodeRepository,
                wayRepository,
                relationRepository,
                3857,
                14)
            .call()
            .size());

    new UpdateService(
            blobStore,
            coordinateCache,
            referenceCache,
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            3857)
        .call();
    assertEquals(2437l, headerRepository.selectLatest().getReplicationSequenceNumber());
  }
}
