package io.gazetteer.postgis;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.core.Tile;
import io.gazetteer.core.TileSource;
import io.gazetteer.core.XYZ;
import mil.nga.sf.GeometryEnvelope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

public class PostgisTileSource implements TileSource {

    private static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";

    private final String DATABASE = "jdbc:postgresql://localhost:5432/osm?user=osm&password=osm";

    private final AsyncLoadingCache<XYZ, Tile> cache;

    private final List<PostgisLayer> layers;

    public PostgisTileSource(List<PostgisLayer> layers) {
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
            for (PostgisLayer layer : layers) {
                tile.write(loadLayer(xyz, layer));
            }
            tile.close();
            return new Tile(data.toByteArray());
        }
    }

    private byte[] loadLayer(XYZ xyz, PostgisLayer layer) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE)) {
            String sql = PostgisQueryBuilder.build(xyz, layer);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            result.next();
            return result.getBytes(1);
        }
    }

}
