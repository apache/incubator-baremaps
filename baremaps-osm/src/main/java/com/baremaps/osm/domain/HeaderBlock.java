package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockConsumer;

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
  public void visit(BlockConsumer handler) throws Exception {
    handler.match(this);
  }

}
