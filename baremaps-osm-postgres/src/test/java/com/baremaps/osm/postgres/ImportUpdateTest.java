package com.baremaps.osm.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.ReferenceCache;
import com.baremaps.osm.database.DatabaseDiffService;
import com.baremaps.osm.database.DatabaseImportService;
import com.baremaps.osm.database.DatabaseUpdateService;
import com.baremaps.osm.cache.InMemoryCache;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Way;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ImportUpdateTest {

  public BlobStore blobStore;
  public DataSource dataSource;
  public PostgresHeaderTable headerTable;
  public PostgresNodeTable nodeTable;
  public PostgresWayTable wayTable;
  public PostgresRelationTable relationTable;

  @BeforeEach
  public void createTable() throws SQLException, IOException, URISyntaxException {
    dataSource = PostgresUtils.datasource(DatabaseConstants.DATABASE_URL);

    blobStore = new ResourceBlobStore();
    headerTable = new PostgresHeaderTable(dataSource);
    nodeTable = new PostgresNodeTable(dataSource);
    wayTable = new PostgresWayTable(dataSource);
    relationTable = new PostgresRelationTable(dataSource);

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
    new DatabaseImportService(
        new URI("res://simple/data.osm.pbf"),
        blobStore,
        new InMemoryCache<>(),
        new InMemoryCache<>(),
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).call();

    headerTable.insert(new Header(0l, LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), "res://simple", "", ""));

    // Check node importation
    assertNull(nodeTable.select(0l));
    assertNotNull(nodeTable.select(1l));
    assertNotNull(nodeTable.select(2l));
    assertNotNull(nodeTable.select(3l));
    assertNull(nodeTable.select(4l));

    // Check way importation
    assertNull(wayTable.select(0l));
    assertNotNull(wayTable.select(1l));
    assertNull(wayTable.select(2l));

    // Check relation importation
    assertNull(relationTable.select(0l));
    assertNotNull(relationTable.select(1l));
    assertNull(relationTable.select(2l));

    // Check node properties
    Node node = nodeTable.select(1l);
    Assertions.assertEquals(1, node.getLon());
    Assertions.assertEquals(1, node.getLat());

    // Check way properties
    Way way = wayTable.select(1l);
    assertNotNull(way);

    // Update the database
    new DatabaseUpdateService(
        blobStore,
        new PostgresCoordinateCache(dataSource),
        new PostgresReferenceCache(dataSource),
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).call();

    // Check deletions
    assertNull(nodeTable.select(0l));
    assertNull(nodeTable.select(1l));

    // Check insertions
    assertNotNull(nodeTable.select(2l));
    assertNotNull(nodeTable.select(3l));
    assertNotNull(nodeTable.select(4l));
  }

  @Test
  @Tag("integration")
  void liechtenstein() throws Exception {

    // Import data
    new DatabaseImportService(
        new URI("res://liechtenstein/liechtenstein.osm.pbf"),
        blobStore,
        new InMemoryCache<>(),
        new InMemoryCache<>(),
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).call();
    assertEquals(2434l, headerTable.latest().getReplicationSequenceNumber());

    // Fix the replicationUrl so that we can update the database with local files
    headerTable.insert(new Header(2434l, LocalDateTime.of(2019, 11, 18, 21, 19, 5, 0), "res://liechtenstein", "", ""));

    CoordinateCache coordinateCache = new PostgresCoordinateCache(dataSource);
    ReferenceCache referenceCache = new PostgresReferenceCache(dataSource);

    assertEquals(0, new DatabaseDiffService(
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857,
        14
    ).call().size());

    // Update the database
    new DatabaseUpdateService(
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).call();
    assertEquals(2435l, headerTable.latest().getReplicationSequenceNumber());

    assertEquals(7, new DatabaseDiffService(
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857,
        14
    ).call().size());

    new DatabaseUpdateService(
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).call();
    assertEquals(2436l, headerTable.latest().getReplicationSequenceNumber());

    assertEquals(0, new DatabaseDiffService(
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857,
        14
    ).call().size());

    new DatabaseUpdateService(
        blobStore,
        coordinateCache,
        referenceCache,
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).call();
    assertEquals(2437l, headerTable.latest().getReplicationSequenceNumber());
  }

}