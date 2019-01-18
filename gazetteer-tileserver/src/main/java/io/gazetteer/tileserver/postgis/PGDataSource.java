package io.gazetteer.tileserver.postgis;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.core.Tile;
import io.gazetteer.core.TileDataSource;
import io.gazetteer.core.XYZ;
import mil.nga.sf.GeometryEnvelope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class PGDataSource implements TileDataSource {

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";

    private final AsyncLoadingCache<XYZ, Tile> cache;

    public final List<PGLayer> layers;

    public PGDataSource(List<PGLayer> layers) {
        this.layers = layers;
        this.cache = Caffeine.newBuilder()
                .maximumSize(10000)
                .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                .buildAsync(xyz -> loadTile(xyz));
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public CompletableFuture<Tile> getTile(XYZ xyz) {
        return cache.get(xyz);
    }

    private Tile loadTile(XYZ xyz) throws IOException, SQLException {
        try (ByteArrayOutputStream data = new ByteArrayOutputStream();
             GZIPOutputStream tile = new GZIPOutputStream(data)) {
            for (PGLayer layer : layers) {
                tile.write(loadLayer(xyz, layer));
            }
            tile.close();
            return new Tile(data.toByteArray());
        }
    }

    private byte[] loadLayer(XYZ xyz, PGLayer layer) throws SQLException {
        try (Connection connection = DriverManager.getConnection(layer.getDatabase())) {
            PreparedStatement statement = connection.prepareStatement(layer.getSql());
            GeometryEnvelope envelope = xyz.envelope();
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
            return result.getBytes(1);
        }
    }

}
