package io.gazetteer.tileserver;

import io.gazetteer.tiles.Coordinate;
import io.gazetteer.mbtiles.MBTilesTile;

import java.util.concurrent.CompletableFuture;

public interface TileDataSource {

    String getMimeType();

    CompletableFuture<MBTilesTile> getTile(Coordinate coordinate);

}
