package io.gazetteer.tilesource;

import java.util.concurrent.CompletableFuture;

public interface TileSource {

  CompletableFuture<Tile> getTile(XYZ xyz);

}
