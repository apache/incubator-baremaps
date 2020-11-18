package com.baremaps.importer.database;

import static com.baremaps.importer.database.DatabaseConstants.DATABASE_URL;

import com.baremaps.util.postgis.PostgisHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class DatabaseImportHandlerTest {

  public DataSource dataSource;
  public HeaderTable headerTable;
  public NodeTable nodeTable;
  public WayTable wayTable;
  public RelationTable relationTable;

  @BeforeEach
  public void createTable() throws SQLException, IOException {
    dataSource = PostgisHelper.poolingDataSource(DATABASE_URL);
    headerTable = new HeaderTable(dataSource);
    nodeTable = new NodeTable(dataSource);
    wayTable = new WayTable(dataSource);
    relationTable = new RelationTable(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      PostgisHelper.execute(connection, "osm_create_extensions.sql");
      PostgisHelper.execute(connection, "osm_drop_tables.sql");
      PostgisHelper.execute(connection, "osm_create_tables.sql");
    }
  }

  @Test
  @Tag("integration")
  void test() throws IOException, DatabaseException, URISyntaxException {
    // Import data
//    try (DatabaseHandler dataImporter = new DatabaseHandler(headerTable, nodeTable, wayTable, relationTable)) {
//      OpenStreetMap.entityStream(TestFiles.dataOsmXml()).forEach(dataImporter);
//    } catch (StreamException e) {
//      e.getCause().printStackTrace();
//    }
//
//    // Check node importation
//    assertNull(nodeTable.select(0l));
//    assertNotNull(nodeTable.select(1l));
//    assertNotNull(nodeTable.select(2l));
//    assertNotNull(nodeTable.select(3l));
//    assertNull(nodeTable.select(4l));
//
//    // Check way importation
//    assertNull(wayTable.select(0l));
//    assertNotNull(wayTable.select(1l));
//    assertNull(wayTable.select(2l));
//
//    // Check relation importation
//    assertNull(relationTable.select(0l));
//    assertNotNull(relationTable.select(1l));
//    assertNull(relationTable.select(2l));
  }


}