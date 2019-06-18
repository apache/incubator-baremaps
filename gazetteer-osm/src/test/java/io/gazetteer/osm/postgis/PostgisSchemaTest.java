package io.gazetteer.osm.postgis;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgisSchemaTest {

  public static final String url = "jdbc:postgresql://localhost:5432/osm?allowMultiQueries=true&user=osm&password=osm";

  @Test
  @Tag("integration")
  public void resetDatabase() throws SQLException, IOException {
    try (Connection connection = DriverManager.getConnection(url)) {
      DatabaseUtil.executeScript(connection, "osm_create_extensions.sql");
      DatabaseUtil.executeScript(connection, "osm_create_tables.sql");
      assertTrue(tableExists("osm_info"));
      assertTrue(tableExists("osm_users"));
      assertTrue(tableExists("osm_nodes"));
      assertTrue(tableExists("osm_ways"));
      assertTrue(tableExists("osm_relations"));
    }
  }

  public boolean tableExists(String table) throws SQLException {
    try (Connection connection = DriverManager.getConnection(url)) {
      DatabaseMetaData metadata = connection.getMetaData();
      ResultSet tables = metadata.getTables(null, null, table, null);
      return tables.next();
    }
  }
}
