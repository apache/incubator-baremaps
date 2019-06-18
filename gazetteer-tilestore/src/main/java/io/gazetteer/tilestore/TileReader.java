package io.gazetteer.tilestore;

public interface TileReader {

  Tile read(XYZ xyz) throws TileException;

}
