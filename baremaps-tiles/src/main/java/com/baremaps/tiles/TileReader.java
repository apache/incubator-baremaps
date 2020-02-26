package com.baremaps.tiles;

public interface TileReader {

  byte[] read(Tile tile) throws TileException;

}
