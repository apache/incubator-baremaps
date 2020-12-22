package com.baremaps.osm.pbf;

import com.baremaps.osm.BlockHandler;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;

public class HeaderBlock extends Block {

  private final Header header;

  private final Bound bound;

  protected HeaderBlock(Blob blob, Header header, Bound bound) {
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
  public void handle(BlockHandler handler) throws Exception {
    handler.handle(this);
  }

}
