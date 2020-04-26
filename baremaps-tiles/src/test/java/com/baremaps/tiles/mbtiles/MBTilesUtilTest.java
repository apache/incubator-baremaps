/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.tiles.mbtiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.util.tile.Tile;
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
    MBTilesUtil.initializeDatabase(connection);
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
    Map<String, String> metadata = MBTilesUtil.readMetadata(connection);
    assertTrue(metadata.size() > 0);
  }

  @Test
  public void getTile() throws SQLException {
    String database = getClass().getClassLoader().getResource("schema.db").getPath();
    Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
    byte[] t1 = MBTilesUtil.readTile(connection, new Tile(0, 0, 0));
    assertNotNull(t1);
    assertThrows(SQLException.class, () -> MBTilesUtil.readTile(connection, new Tile(1, 1, 1)));
  }

  @Test
  public void setMetadata() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    MBTilesUtil.initializeDatabase(connection);
    Map<String, String> m1 = new HashMap<>();
    m1.put("test", "test");
    MBTilesUtil.writeMetadata(connection, m1);
    Map<String, String> m2 = MBTilesUtil.readMetadata(connection);
    assertTrue(m2.size() > 0);
  }

  @Test
  public void setTile() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    MBTilesUtil.initializeDatabase(connection);
    MBTilesUtil.writeTile(connection, new Tile(0, 0, 0), "test".getBytes());
    assertNotNull(MBTilesUtil.readTile(connection, new Tile(0, 0, 0)));
  }

}
