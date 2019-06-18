package io.gazetteer.tilesource;

public interface TileReader {

  Tile read(XYZ xyz) throws TileException;

}
