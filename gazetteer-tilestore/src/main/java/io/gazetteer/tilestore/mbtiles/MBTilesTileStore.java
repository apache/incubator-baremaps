package io.gazetteer.tilestore.mbtiles;

import io.gazetteer.tilestore.Tile;
import io.gazetteer.tilestore.TileException;
import io.gazetteer.tilestore.TileReader;
import io.gazetteer.tilestore.TileWriter;
import io.gazetteer.tilestore.XYZ;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class MBTilesTileStore implements TileReader, TileWriter {

  public final org.sqlite.SQLiteDataSource dataSource;

  public final Map<String, String> metadata;

  public MBTilesTileStore(org.sqlite.SQLiteDataSource dataSource, Map<String, String> metadata) {
    this(dataSource, metadata, 10000);
  }

  public MBTilesTileStore(
      org.sqlite.SQLiteDataSource dataSource, Map<String, String> metadata, int cacheSize) {
    this.dataSource = dataSource;
    this.metadata = metadata;

  }

  @Override
  public Tile read(XYZ xyz) throws TileException {
    try (Connection connection = dataSource.getConnection()) {
      return MBTilesUtil.getTile(connection, xyz);
    } catch (SQLException e) {
      throw new TileException(e);
    }
  }

  public static MBTilesTileStore fromDataSource(org.sqlite.SQLiteDataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      Map<String, String> metadata = MBTilesUtil.getMetadata(connection);
      return new MBTilesTileStore(dataSource, metadata);
    }
  }

  @Override
  public void write(XYZ xyz, Tile tile) throws TileException {
    try (Connection connection = dataSource.getConnection()) {
      MBTilesUtil.setTile(connection, xyz, tile);
    } catch (SQLException e) {
      throw new TileException(e);
    }
  }
}
