package io.gazetteer.mbtiles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MBTiles {

    public static void createDatabase(Connection connection) throws SQLException {
        try(PreparedStatement metadata = connection.prepareStatement("CREATE TABLE metadata (name TEXT, value TEXT, PRIMARY KEY (name))");
            PreparedStatement tiles = connection.prepareStatement("CREATE TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB)");
            PreparedStatement coord = connection.prepareStatement("CREATE UNIQUE INDEX coord ON tiles (zoom_level, tile_column, tile_row)")) {
            metadata.executeUpdate();
            tiles.executeUpdate();
            coord.executeUpdate();
        }
    }

    public static Map<String, String> getMetadata(Connection connection) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement("SELECT name, value FROM metadata;");
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

    public static Tile getTile(Connection connection, Coordinate coord) throws SQLException {
        try(PreparedStatement statement = getTileStatement(connection, coord);
            ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return new Tile(resultSet.getBytes("tile_data"));
            } else {
                return null;
            }
        }
    }

    private static PreparedStatement getTileStatement(Connection connection, Coordinate coord) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT tile_data FROM tiles WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?");
        statement.setInt(1, coord.zoom);
        statement.setInt(2, coord.x);
        statement.setInt(3, invertY(coord.y, coord.zoom));
        return statement;
    }

    public static void setMetadata(Connection connection, Map<String, String> metadata) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement("INSERT INTO metadata (name, value) VALUES (?, ?)")) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                statement.setString(1, entry.getKey());
                statement.setString(2, entry.getValue());
                statement.executeUpdate();
            }
        }
    }

    public static void setTile(Connection connection, Coordinate coord, Tile tile) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement("INSERT INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, coord.zoom);
            statement.setInt(2, coord.x);
            statement.setInt(3, coord.y);
            statement.setBytes(4, tile.bytes);
            statement.executeUpdate();
        }
    }


    /**
     * Invert a Y coordinate between TMS tile and Google tile origins.
     */
    private static int invertY(int y, int z) {
        return (int) (Math.pow(2.0, z) - 1 - y);
    }

}
