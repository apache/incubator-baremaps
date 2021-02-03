package com.baremaps.osm.pbf;

import com.baremaps.osm.BlockHandler;

public abstract class Block {

  private final Blob blob;

  public Block(Blob blob) {
    this.blob = blob;
  }

  public Blob getBlob() {
    return blob;
  }

  public abstract void handle(BlockHandler handler) throws Exception;

}
