package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockConsumer;
import com.baremaps.osm.handler.BlockFunction;

/**
 * Represents a block of data in an OpenStreetMap dataset.
 */
public abstract class Block {

  private final Blob blob;

  public Block(Blob blob) {
    this.blob = blob;
  }

  public Blob getBlob() {
    return blob;
  }

  public abstract void visit(BlockConsumer consumer) throws Exception;

  public abstract <T> T visit(BlockFunction<T> function) throws Exception;
}
