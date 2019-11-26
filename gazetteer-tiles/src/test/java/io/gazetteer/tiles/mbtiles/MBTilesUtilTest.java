package io.gazetteer.tiles.mbtiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gazetteer.tiles.Tile;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class MBTilesUtilTest {

  @Test
  public void createDatabase() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    MBTilesUtil.createDatabase(connection);
    DatabaseMetaData md = connection.getMetaData();
    ResultSet rs1 = md.getTables(null, null, "metadata", null);
    assertTrue(rs1.next());
    assertTrue(rs1.getRow() > 0);
    ResultSet rs2 = md.getTables(null, null, "tiles", null);
    assertTrue(rs2.next());
    assertTrue(rs2.getRow() > 0);
  }

  @Test
  public void getMetadata() throws SQLException {
    String database = getClass().getClassLoader().getResource("schema.db").getPath();
    Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
    Map<String, String> metadata = MBTilesUtil.getMetadata(connection);
    assertTrue(metadata.size() > 0);
  }

  @Test
  public void getTile() throws SQLException {
    String database = getClass().getClassLoader().getResource("schema.db").getPath();
    Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
    byte[] t1 = MBTilesUtil.getTile(connection, new Tile(0, 0, 0));
    assertNotNull(t1);
    byte[] t2 = MBTilesUtil.getTile(connection, new Tile(1, 1, 1));
    assertNull(t2);
  }

  @Test
  public void setMetadata() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    MBTilesUtil.createDatabase(connection);
    Map<String, String> m1 = new HashMap<>();
    m1.put("test", "test");
    MBTilesUtil.setMetadata(connection, m1);
    Map<String, String> m2 = MBTilesUtil.getMetadata(connection);
    assertTrue(m2.size() > 0);
  }

  @Test
  public void setTile() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    MBTilesUtil.createDatabase(connection);
    MBTilesUtil.setTile(connection, new Tile(0, 0, 0), "test".getBytes());
    assertNotNull(MBTilesUtil.getTile(connection, new Tile(0, 0, 0)));
  }
}
