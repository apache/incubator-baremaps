package io.gazetteer.tilesource;

public interface TileTarget {

  void setTile(XYZ xyz, Tile tile) throws TileException;

}
