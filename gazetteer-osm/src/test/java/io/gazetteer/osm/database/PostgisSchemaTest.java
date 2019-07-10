package io.gazetteer.osm.database;

import io.gazetteer.common.postgis.util.DatabaseUtil;
import java.io.IOException;

import io.gazetteer.osm.OSMTestUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgisSchemaTest {

  @Test
  @Tag("integration")
  public void resetDatabase() throws SQLException, IOException {
    try (Connection connection = DriverManager.getConnection(OSMTestUtil.DATABASE_URL)) {
      DatabaseUtil.executeScript(connection, "osm_create_extensions.sql");
      DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
      assertTrue(tableExists("osm_headers"));
      assertTrue(tableExists("osm_nodes"));
      assertTrue(tableExists("osm_ways"));
      assertTrue(tableExists("osm_relations"));
    }
  }

  public boolean tableExists(String table) throws SQLException {
    try (Connection connection = DriverManager.getConnection(OSMTestUtil.DATABASE_URL)) {
      DatabaseMetaData metadata = connection.getMetaData();
      ResultSet tables = metadata.getTables(null, null, table, null);
      return tables.next();
    }
  }
}
