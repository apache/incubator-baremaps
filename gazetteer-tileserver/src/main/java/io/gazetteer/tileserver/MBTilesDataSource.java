package io.gazetteer.tileserver;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.gazetteer.tiles.Coordinate;
import io.gazetteer.mbtiles.MBTilesDatabase;
import io.gazetteer.mbtiles.MBTilesMetadata;
import io.gazetteer.mbtiles.MBTilesTile;
import mil.nga.sf.GeometryEnvelope;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class MBTilesDataSource implements TileDataSource {

    public final SQLiteDataSource dataSource;

    public final MBTilesMetadata metadata;

    public final int cacheSize;

    private final AsyncLoadingCache<Coordinate, MBTilesTile> cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2))
            .buildAsync(coord -> loadTile(coord));

    public MBTilesDataSource(SQLiteDataSource dataSource, MBTilesMetadata metadata) {
        this(dataSource, metadata, 10000);
    }

    public MBTilesDataSource(SQLiteDataSource dataSource, MBTilesMetadata metadata, int cacheSize) {
        this.dataSource = dataSource;
        this.metadata = metadata;
        this.cacheSize = cacheSize;
    }

    public String getMimeType() {
        return metadata.format.mimeType;
    }

    private MBTilesTile loadTile(Coordinate coordinate) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return MBTilesDatabase.getTile(connection, coordinate);
        }
    }

    @Override
    public CompletableFuture<MBTilesTile> getTile(Coordinate coordinate) {
        return cache.get(coordinate);
    }

    public static MBTilesDataSource fromDataSource(SQLiteDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> map = MBTilesDatabase.getMetadata(connection);
            MBTilesMetadata metadata = MBTilesMetadata.fromMap(map);
            return new MBTilesDataSource(dataSource, metadata);
        }
    }


}
