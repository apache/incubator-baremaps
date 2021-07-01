package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockConsumer;

public abstract class Block {

  private final Blob blob;

  public Block(Blob blob) {
    this.blob = blob;
  }

  public Blob getBlob() {
    return blob;
  }

  public abstract void visit(BlockConsumer handler) throws Exception;

}
