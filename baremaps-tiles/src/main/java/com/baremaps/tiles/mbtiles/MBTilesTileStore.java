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

import com.baremaps.tiles.TileStore;
import com.baremaps.util.tile.Tile;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class MBTilesTileStore implements TileStore {

  private final org.sqlite.SQLiteDataSource dataSource;

  public MBTilesTileStore(org.sqlite.SQLiteDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public byte[] read(Tile tile) throws IOException {
    try (Connection connection = dataSource.getConnection()) {
      return MBTilesUtil.readTile(connection, tile);
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void write(Tile tile, byte[] bytes) throws IOException {
    try (Connection connection = dataSource.getConnection()) {
      MBTilesUtil.writeTile(connection, tile, bytes);
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void delete(Tile tile) throws IOException {
    try (Connection connection = dataSource.getConnection()) {
      MBTilesUtil.deleteTile(connection, tile);
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  public void initializeDatabase() throws IOException {
    try (Connection connection = dataSource.getConnection()) {
      MBTilesUtil.initializeDatabase(connection);
    } catch (SQLException ex) {
      throw new IOException(ex);
    }
  }

  public Map<String, String> readMetadata() throws IOException {
    try (Connection connection = dataSource.getConnection()) {
      return MBTilesUtil.readMetadata(connection);
    } catch (SQLException ex) {
      throw new IOException(ex);
    }
  }

  public void writeMetadata(Map<String, String> metadata) throws IOException {
    try (Connection connection = dataSource.getConnection()) {
      MBTilesUtil.truncateMetadata(connection);
      MBTilesUtil.writeMetadata(connection, metadata);
    } catch (SQLException ex) {
      throw new IOException(ex);
    }
  }

}
