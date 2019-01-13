package io.gazetteer.tileserver;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.mbtiles.MBTilesCoordinate;
import io.gazetteer.mbtiles.MBTilesDatabase;
import io.gazetteer.mbtiles.MBTilesMetadata;
import io.gazetteer.mbtiles.MBTilesTile;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class TileServerCache {

    public final SQLiteDataSource dataSource;

    public final MBTilesMetadata metadata;

    public final int cacheSize;

    private final AsyncLoadingCache<MBTilesCoordinate, MBTilesTile> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
            .buildAsync(coord -> loadTile(coord));

    public TileServerCache(SQLiteDataSource dataSource, MBTilesMetadata metadata) {
        this(dataSource, metadata, 10000);
    }

    public TileServerCache(SQLiteDataSource dataSource, MBTilesMetadata metadata, int cacheSize) {
        this.dataSource = dataSource;
        this.metadata = metadata;
        this.cacheSize = cacheSize;
    }

    private MBTilesTile loadTile(MBTilesCoordinate coord) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return MBTilesDatabase.getTile(connection, coord);
        }
    }

    public CompletableFuture<MBTilesTile> getTile(MBTilesCoordinate coord) {
        return cache.get(coord);
    }

    public static TileServerCache fromDataSource(SQLiteDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> map = MBTilesDatabase.getMetadata(connection);
            MBTilesMetadata metadata = MBTilesMetadata.fromMap(map);
            return new TileServerCache(dataSource, metadata);
        }
    }

}
