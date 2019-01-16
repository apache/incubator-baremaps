package io.gazetteer.tileserver.postgis;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.mbtiles.XYZ;
import io.gazetteer.mbtiles.Tile;
import io.gazetteer.tileserver.TileDataSource;
import mil.nga.sf.GeometryEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class PGDataSource implements TileDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PGDataSource.class);

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";

    private final AsyncLoadingCache<XYZ, Tile> cache;

    public final List<PGLayer> layers;

    public PGDataSource(List<PGLayer> layers) {
        this.layers = layers;
        this.cache = Caffeine.newBuilder()
                .maximumSize(10000)
                .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                .buildAsync(coord -> loadTile(coord));
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public CompletableFuture<Tile> getTile(XYZ coordinates) {
        return cache.get(coordinates);
    }

    private Tile loadTile(XYZ coordinates) throws IOException, SQLException {
        try (ByteArrayOutputStream data = new ByteArrayOutputStream();
             GZIPOutputStream tile = new GZIPOutputStream(data)) {
            for (PGLayer layer : layers) {
                tile.write(loadLayer(coordinates, layer));
            }
            tile.close();
            return new Tile(data.toByteArray());
        }
    }

    private byte[] loadLayer(XYZ coordinates, PGLayer layer) throws SQLException {
        try (Connection connection = DriverManager.getConnection(layer.getDatabase())) {
            PreparedStatement statement = connection.prepareStatement(layer.getSql());
            GeometryEnvelope envelope = coordinates.envelope();
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
