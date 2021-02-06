package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;

public interface ElementHandler extends EntityHandler {

  @Override
  default void handle(Header header) {
    // Do nothing
  }

  @Override
  default void handle(Bound bound) {
    // Do nothing
  }

}
