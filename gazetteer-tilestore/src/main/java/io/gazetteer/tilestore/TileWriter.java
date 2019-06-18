package io.gazetteer.tilestore;

public interface TileWriter {

  void write(XYZ xyz, Tile tile) throws TileException;

}
