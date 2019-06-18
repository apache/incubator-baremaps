package io.gazetteer.tilesource;

public interface TileWriter {

  void write(XYZ xyz, Tile tile) throws TileException;

}
