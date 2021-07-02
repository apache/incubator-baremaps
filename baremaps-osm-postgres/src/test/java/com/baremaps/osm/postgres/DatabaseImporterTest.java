package com.baremaps.osm.postgres;

import static com.baremaps.osm.postgres.DatabaseConstants.DATABASE_URL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.blob.ResourceBlobStore;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.database.DatabaseException;
import com.baremaps.osm.database.DatabaseImportConsumer;
import com.baremaps.postgres.jdbc.PostgresUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class DatabaseImporterTest {

  public DataSource dataSource;
  public PostgresHeaderTable headerTable;
  public PostgresNodeTable nodeTable;
  public PostgresWayTable wayTable;
  public PostgresRelationTable relationTable;

  @BeforeEach
   void createTable() throws SQLException, IOException {
    dataSource = PostgresUtils.datasource(DATABASE_URL);
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
  void test() throws IOException, DatabaseException, URISyntaxException {
    // Import data
    DatabaseImportConsumer dataImporter = new DatabaseImportConsumer(headerTable, nodeTable, wayTable, relationTable);
    OpenStreetMap.streamPbfBlocks(new ResourceBlobStore().read(new URI("res://simple/data.osm.pbf"))).forEach(dataImporter);

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
  }


}