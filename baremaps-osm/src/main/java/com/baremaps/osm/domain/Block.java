package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockHandler;

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
