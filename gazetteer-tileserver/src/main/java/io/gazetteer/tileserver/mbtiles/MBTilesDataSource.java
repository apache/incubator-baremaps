package io.gazetteer.tileserver.mbtiles;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.mbtiles.Coordinate;
import io.gazetteer.mbtiles.MBTiles;
import io.gazetteer.mbtiles.Metadata;
import io.gazetteer.mbtiles.Tile;
import io.gazetteer.tileserver.TileDataSource;
import io.gazetteer.tileserver.postgis.PGDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class MBTilesDataSource implements TileDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBTilesDataSource.class);

    public final SQLiteDataSource dataSource;

    public final Metadata metadata;

    private final AsyncLoadingCache<Coordinate, Tile> cache;

    public MBTilesDataSource(SQLiteDataSource dataSource, Metadata metadata) {
        this(dataSource, metadata, 10000);
    }

    public MBTilesDataSource(SQLiteDataSource dataSource, Metadata metadata, int cacheSize) {
        this.dataSource = dataSource;
        this.metadata = metadata;
        this.cache = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
                .buildAsync(coord -> loadTile(coord));
    }

    public String getMimeType() {
        return metadata.format.mimeType;
    }

    private Tile loadTile(Coordinate coordinate) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return MBTiles.getTile(connection, coordinate);
        }
    }

    @Override
    public CompletableFuture<Tile> getTile(Coordinate coordinate) {
        return cache.get(coordinate);
    }

    public static MBTilesDataSource fromDataSource(SQLiteDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> map = MBTiles.getMetadata(connection);
            Metadata metadata = Metadata.fromMap(map);
            return new MBTilesDataSource(dataSource, metadata);
        }
    }


}
