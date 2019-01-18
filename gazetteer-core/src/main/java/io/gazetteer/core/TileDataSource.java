package io.gazetteer.core;

import java.util.concurrent.CompletableFuture;

public interface TileDataSource {

    String getMimeType();

    CompletableFuture<Tile> getTile(XYZ xyz);

}
