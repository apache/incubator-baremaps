package io.gazetteer.tileserver.postgis;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.mbtiles.Tile;
import io.gazetteer.mbtiles.Coordinate;
import io.gazetteer.tileserver.TileDataSource;
import mil.nga.sf.GeometryEnvelope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class PostgisDataSource implements TileDataSource {

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";

    public final int cacheSize;

    private final AsyncLoadingCache<Coordinate, Tile> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
            .buildAsync(coord -> loadTile(coord));

    public final List<PostgisLayer> layers;

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
            ") AS q;";

    public PostgisDataSource(List<PostgisLayer> layers) {
        this.layers = layers;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public CompletableFuture<Tile> getTile(Coordinate coordinate) {
        try (GZIPOutputStream data = new GZIPOutputStream(new ByteArrayOutputStream())) {
            for (PostgisLayer layer : layers) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }



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
                return CompletableFuture.completedFuture(new Tile(gzip(result.getBytes(1))));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public

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
