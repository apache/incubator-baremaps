package com.baremaps.tiles.mbtiles;

import com.baremaps.tiles.Tile;
import com.baremaps.tiles.TileException;
import com.baremaps.tiles.TileReader;
import com.baremaps.tiles.TileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class MBTilesStore implements TileReader, TileWriter {

  public final org.sqlite.SQLiteDataSource dataSource;

  public final Map<String, String> metadata;

  public MBTilesStore(org.sqlite.SQLiteDataSource dataSource, Map<String, String> metadata) {
    this(dataSource, metadata, 10000);
  }

  public MBTilesStore(
      org.sqlite.SQLiteDataSource dataSource, Map<String, String> metadata, int cacheSize) {
    this.dataSource = dataSource;
    this.metadata = metadata;

  }

  @Override
  public byte[] read(Tile tile) throws TileException {
    try (Connection connection = dataSource.getConnection()) {
      return MBTilesUtil.getTile(connection, tile);
    } catch (SQLException e) {
      throw new TileException(e);
    }
  }

  public static MBTilesStore fromDataSource(org.sqlite.SQLiteDataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      Map<String, String> metadata = MBTilesUtil.getMetadata(connection);
      return new MBTilesStore(dataSource, metadata);
    }
  }

  @Override
  public void write(Tile tile, byte[] bytes) throws TileException {
    try (Connection connection = dataSource.getConnection()) {
      MBTilesUtil.setTile(connection, tile, bytes);
    } catch (SQLException e) {
      throw new TileException(e);
    }
  }
}
