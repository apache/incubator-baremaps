package io.gazetteer.tileserver.mbtiles;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.mbtiles.XYZ;
import io.gazetteer.mbtiles.MBTiles;
import io.gazetteer.mbtiles.Tile;
import io.gazetteer.tileserver.TileDataSource;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class MBTilesDataSource implements TileDataSource {

    public static final String MIME_TYPE = "application/vnd.mapbox-vector-tile";

    public final SQLiteDataSource dataSource;

    public final Map<String, String> metadata;

    private final AsyncLoadingCache<XYZ, Tile> cache;

    public MBTilesDataSource(SQLiteDataSource dataSource, Map<String, String> metadata) {
        this(dataSource, metadata, 10000);
    }

    public MBTilesDataSource(SQLiteDataSource dataSource, Map<String, String> metadata, int cacheSize) {
        this.dataSource = dataSource;
        this.metadata = metadata;
        this.cache = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                .buildAsync(xyz -> loadTile(xyz));
    }

    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public CompletableFuture<Tile> getTile(XYZ xyz) {
        return cache.get(xyz);
    }

    private Tile loadTile(XYZ xyz) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return MBTiles.getTile(connection, xyz);
        }
    }

    public static MBTilesDataSource fromDataSource(SQLiteDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> metadata = MBTiles.getMetadata(connection);
            return new MBTilesDataSource(dataSource, metadata);
        }
    }


}
