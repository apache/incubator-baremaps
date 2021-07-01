package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockConsumer;
import com.baremaps.osm.handler.BlockFunction;

/**
 * Represents a header block in an OpenStreetMap dataset.
 */
public class HeaderBlock extends Block {

  private final Header header;

  private final Bound bound;

  public HeaderBlock(Blob blob, Header header, Bound bound) {
    super(blob);
    this.header = header;
    this.bound = bound;
  }

  public Header getHeader() {
    return header;
  }

  public Bound getBound() {
    return bound;
  }

  @Override
  public void visit(BlockConsumer consumer) throws Exception {
    consumer.match(this);
  }

  @Override
  public <T> T visit(BlockFunction<T> function) throws Exception {
    return function.match(this);
  }

}
