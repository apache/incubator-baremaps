package io.gazetteer.mbtiles;

import io.gazetteer.core.Tile;
import io.gazetteer.core.XYZ;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MBTilesUtil {

  private static final String CREATE_TABLE_METADATA =
      "CREATE TABLE metadata (name TEXT, value TEXT, PRIMARY KEY (name))";

  private static final String CREATE_TABLE_TILES =
      "CREATE TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB)";

  private static final String CREATE_INDEX_TILES =
      "CREATE UNIQUE INDEX tile_index on tiles (zoom_level, tile_column, tile_row)";

  private static final String SELECT_METADATA = "SELECT name, value FROM metadata";

  private static final String SELECT_TILE =
      "SELECT tile_data FROM tiles WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?";

  private static final String INSERT_METADATA = "INSERT INTO metadata (name, value) VALUES (?, ?)";

  private static final String INSERT_TILE =
      "INSERT INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)";

  public static void createDatabase(Connection connection) throws SQLException {
    checkNotNull(connection);
    try (Statement statement = connection.createStatement()) {
      statement.execute(CREATE_TABLE_METADATA);
      statement.execute(CREATE_TABLE_TILES);
      statement.execute(CREATE_INDEX_TILES);
    }
  }

  public static Map<String, String> getMetadata(Connection connection) throws SQLException {
    checkNotNull(connection);
    try (PreparedStatement statement = connection.prepareStatement(SELECT_METADATA);
        ResultSet resultSet = statement.executeQuery()) {
      Map<String, String> metadata = new HashMap<>();
      while (resultSet.next()) {
        String name = resultSet.getString("name");
        String value = resultSet.getString("value");
        metadata.put(name, value);
      }
      return metadata;
    }
  }

  public static Tile getTile(Connection connection, XYZ xyz) throws SQLException {
    checkNotNull(connection);
    checkNotNull(xyz);
    try (PreparedStatement statement = getTileStatement(connection, xyz);
        ResultSet resultSet = statement.executeQuery()) {
      if (resultSet.next()) {
        return new Tile(resultSet.getBytes("tile_data"));
      } else {
        return null;
      }
    }
  }

  private static PreparedStatement getTileStatement(Connection connection, XYZ xyz)
      throws SQLException {
    checkNotNull(connection);
    checkNotNull(xyz);
    PreparedStatement statement = connection.prepareStatement(SELECT_TILE);
    statement.setInt(1, xyz.getZ());
    statement.setInt(2, xyz.getX());
    statement.setInt(3, reverseY(xyz.getY(), xyz.getZ()));
    return statement;
  }

  public static void setMetadata(Connection connection, Map<String, String> metadata)
      throws SQLException {
    checkNotNull(connection);
    checkNotNull(metadata);
    try (PreparedStatement statement = connection.prepareStatement(INSERT_METADATA)) {
      for (Map.Entry<String, String> entry : metadata.entrySet()) {
        statement.setString(1, entry.getKey());
        statement.setString(2, entry.getValue());
        statement.executeUpdate();
      }
    }
  }

  public static void setTile(Connection connection, XYZ xyz, Tile tile) throws SQLException {
    checkNotNull(connection);
    checkNotNull(xyz);
    checkNotNull(tile);
    try (PreparedStatement statement = connection.prepareStatement(INSERT_TILE)) {
      statement.setInt(1, xyz.getZ());
      statement.setInt(2, xyz.getX());
      statement.setInt(3, xyz.getY());
      statement.setBytes(4, tile.getBytes());
      statement.executeUpdate();
    }
  }

  private static int reverseY(int y, int z) {
    return (int) (Math.pow(2.0, z) - 1 - y);
  }
}
