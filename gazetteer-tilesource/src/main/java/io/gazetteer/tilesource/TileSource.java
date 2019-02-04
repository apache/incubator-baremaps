package io.gazetteer.tilesource;

import java.util.concurrent.CompletableFuture;

public interface TileSource {

  String getMimeType();

  CompletableFuture<Tile> getTile(XYZ xyz);
}
