package com.baremaps.tiles;

public interface TileWriter {

  void write(Tile tile, byte[] bytes) throws TileException;

}
