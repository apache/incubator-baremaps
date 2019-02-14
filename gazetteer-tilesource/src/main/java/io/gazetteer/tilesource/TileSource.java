package io.gazetteer.tilesource;

import java.util.concurrent.CompletableFuture;

public interface TileSource {

  String getStyle();

  CompletableFuture<Tile> getTile(XYZ xyz);

}
