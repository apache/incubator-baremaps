package io.gazetteer.tileserver;

import io.gazetteer.mbtiles.MBTilesTile;
import io.gazetteer.tiles.Coordinate;
import mil.nga.sf.GeometryEnvelope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

public class PGTilesDataSource implements TileDataSource {

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";

    public static final String SELECT_MVT =
            "SELECT ST_AsMVT(q, 'buildings', 4096, 'geom')\n" +
            "FROM (\n" +
            "  SELECT id,\n" +
            "    ST_AsMvtGeom(\n" +
            "      geom,\n" +
            "      ST_MakeEnvelope(?, ?, ?, ?),\n" +
            "      4096,\n" +
            "      256,\n" +
            "      true\n" +
            "    ) AS geom\n" +
            "  FROM ways\n" +
            "  WHERE geom && ST_MakeEnvelope(?, ?, ?, ?)\n" +
            "  AND ST_Intersects(geom, ST_MakeEnvelope(?, ?, ?, ?))\n" +
            "  AND tags -> 'building' = 'yes'" +
            ") AS q;";
    
    
    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public CompletableFuture<MBTilesTile> getTile(Coordinate coordinate) {

        try {
            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/osm?user=osm&password=osm")) {
                PreparedStatement statement = connection.prepareStatement(SELECT_MVT);
                GeometryEnvelope envelope = coordinate.envelope();
                statement.setDouble(1, envelope.getMinX());
                statement.setDouble(2, envelope.getMinY());
                statement.setDouble(3, envelope.getMaxX());
                statement.setDouble(4, envelope.getMaxY());
                statement.setDouble(5, envelope.getMinX());
                statement.setDouble(6, envelope.getMinY());
                statement.setDouble(7, envelope.getMaxX());
                statement.setDouble(8, envelope.getMaxY());
                statement.setDouble(9, envelope.getMinX());
                statement.setDouble(10, envelope.getMinY());
                statement.setDouble(11, envelope.getMaxX());
                statement.setDouble(12, envelope.getMaxY());
                ResultSet result = statement.executeQuery();
                result.next();
                return CompletableFuture.completedFuture(new MBTilesTile(gzip(result.getBytes(1))));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] gzip(byte[] bytes) {
        byte[] result = new byte[]{};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
             GZIPOutputStream gos = new GZIPOutputStream(bos)) {
            gos.write(bytes);
            gos.close();
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
