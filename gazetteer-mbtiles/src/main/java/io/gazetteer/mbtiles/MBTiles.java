package io.gazetteer.mbtiles;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MBTiles {

    private static final String CREATE_TABLE_METADATA = "CREATE TABLE metadata (name TEXT, value TEXT, PRIMARY KEY (name))";

    private static final String CREATE_TABLE_TILES = "CREATE TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB)";

    private static final String CREATE_INDEX_TILES = "CREATE UNIQUE INDEX tile_index on tiles (zoom_level, tile_column, tile_row)";

    private static final String SELECT_METADATA = "SELECT name, value FROM metadata";

    private static final String SELECT_TILE = "SELECT tile_data FROM tiles WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?";

    private static final String INSERT_METADATA = "INSERT INTO metadata (name, value) VALUES (?, ?)";

    private static final String INSERT_TILE = "INSERT INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)";

    public static void createDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_METADATA);
            statement.execute(CREATE_TABLE_TILES);
            statement.execute(CREATE_INDEX_TILES);
        }
    }

    public static Map<String, String> getMetadata(Connection connection) throws SQLException {
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

    public static Tile getTile(Connection connection, XYZ coordinates) throws SQLException {
        try (PreparedStatement statement = getTileStatement(connection, coordinates);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return new Tile(resultSet.getBytes("tile_data"));
            } else {
                return null;
            }
        }
    }

    private static PreparedStatement getTileStatement(Connection connection, XYZ coordinates) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SELECT_TILE);
        statement.setInt(1, coordinates.getZ());
        statement.setInt(2, coordinates.getX());
        statement.setInt(3, reverseY(coordinates.getY(), coordinates.getZ()));
        return statement;
    }

    public static void setMetadata(Connection connection, Map<String, String> metadata) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_METADATA)) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                statement.setString(1, entry.getKey());
                statement.setString(2, entry.getValue());
                statement.executeUpdate();
            }
        }
    }

    public static void setTile(Connection connection, XYZ coordinates, Tile tile) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TILE)) {
            statement.setInt(1, coordinates.getZ());
            statement.setInt(2, coordinates.getX());
            statement.setInt(3, coordinates.getY());
            statement.setBytes(4, tile.getBytes());
            statement.executeUpdate();
        }
    }

    private static int reverseY(int y, int z) {
        return (int) (Math.pow(2.0, z) - 1 - y);
    }

}
