package io.gazetteer.tileserver;

import io.gazetteer.mbtiles.Coordinate;
import io.gazetteer.mbtiles.Tile;

import java.util.concurrent.CompletableFuture;

public interface TileDataSource {

    String getMimeType();

    CompletableFuture<Tile> getTile(Coordinate coordinate);

}
