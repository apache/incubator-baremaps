package io.gazetteer.tilesource;

public interface TileSource {

  Tile getTile(XYZ xyz) throws TileException;

}
