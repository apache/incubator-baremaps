package com.baremaps.osm.postgres;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.FileBlobStore;
import com.baremaps.osm.ImportTask;
import com.baremaps.osm.UpdateTask;
import com.baremaps.osm.cache.InMemoryCache;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
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
    dataSource = PostgresHelper.datasource(DatabaseConstants.DATABASE_URL);

    blobStore = new FileBlobStore();
    headerTable = new PostgresHeaderTable(dataSource);
    nodeTable = new PostgresNodeTable(dataSource);
    wayTable = new PostgresWayTable(dataSource);
    relationTable = new PostgresRelationTable(dataSource);

    try (Connection connection = dataSource.getConnection()) {
      PostgresHelper.executeResource(connection, "osm_create_extensions.sql");
      PostgresHelper.executeResource(connection, "osm_drop_tables.sql");
      PostgresHelper.executeResource(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void test() throws Exception {
    Node node;
    Way way;

    // Import data
    new ImportTask(
        getClass().getClassLoader().getResource("update.osm.pbf").toURI(),
        blobStore,
        new InMemoryCache<>(),
        new InMemoryCache<>(),
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).execute();

    headerTable.insert(new Header(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0), 0l, "target/test-classes", "", ""));

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
    node = nodeTable.select(1l);
    Assertions.assertEquals(1, node.getLon());
    Assertions.assertEquals(1, node.getLat());

    // Check way properties
    way = wayTable.select(1l);
    assertNotNull(way);

    new UpdateTask(
        blobStore,
        new PostgresCoordinateCache(dataSource),
        new PostgresReferenceCache(dataSource),
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857,
        1
    ).execute();

    assertNull(nodeTable.select(0l));
    assertNull(nodeTable.select(1l));
    assertNotNull(nodeTable.select(2l));
    assertNotNull(nodeTable.select(3l));
    assertNotNull(nodeTable.select(4l));
  }

}