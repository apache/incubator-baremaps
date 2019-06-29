package io.gazetteer.tilestore.model;

public interface TileWriter {

  void write(XYZ xyz, Tile tile) throws TileException;

}
