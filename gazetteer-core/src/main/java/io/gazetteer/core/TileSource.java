package io.gazetteer.core;

import java.util.concurrent.CompletableFuture;

public interface TileSource {

  String getMimeType();

  CompletableFuture<Tile> getTile(XYZ xyz);
}
