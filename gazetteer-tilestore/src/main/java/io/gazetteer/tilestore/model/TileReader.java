package io.gazetteer.tilestore.model;

public interface TileReader {

  Tile read(XYZ xyz) throws TileException;

}
