package io.gazetteer.tiles;

public interface TileWriter {

  void write(Tile tile, byte[] bytes) throws TileException;

}
