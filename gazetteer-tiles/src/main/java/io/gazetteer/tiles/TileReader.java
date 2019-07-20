package io.gazetteer.tiles;

public interface TileReader {

  byte[] read(Tile tile) throws TileException;

}
