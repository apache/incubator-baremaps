package com.baremaps.osm;

import static com.baremaps.osm.database.DatabaseConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.osm.cache.InMemoryCache;
import com.baremaps.osm.cache.PostgresCoordinateCache;
import com.baremaps.osm.cache.PostgresReferenceCache;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Way;
import com.baremaps.core.postgres.PostgresHelper;
import com.baremaps.core.storage.BlobStore;
import com.baremaps.core.storage.FileBlobStore;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ImportUpdateTest {

  public BlobStore blobStore;
  public DataSource dataSource;
  public HeaderTable headerTable;
  public NodeTable nodeTable;
  public WayTable wayTable;
  public RelationTable relationTable;

  @BeforeEach
  public void createTable() throws SQLException, IOException, URISyntaxException {
    dataSource = PostgresHelper.datasource(DATABASE_URL);

    blobStore = new FileBlobStore();
    headerTable = new HeaderTable(dataSource);
    nodeTable = new NodeTable(dataSource);
    wayTable = new WayTable(dataSource);
    relationTable = new RelationTable(dataSource);

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

    headerTable.insert(new Header(LocalDateTime.of(2020, 1, 1, 0,0,0,0), 0l, "target/test-classes", "", ""));

    // Import data
    new ImportTask(
        TestFiles.updateOsmPbf(),
        blobStore,
        new InMemoryCache<>(),
        new InMemoryCache<>(),
        headerTable,
        nodeTable,
        wayTable,
        relationTable,
        3857
    ).execute();

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
    assertEquals(1, node.getLon());
    assertEquals(1, node.getLat());

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