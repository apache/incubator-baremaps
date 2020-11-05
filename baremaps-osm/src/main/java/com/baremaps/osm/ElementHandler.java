package com.baremaps.osm;

import com.baremaps.osm.domain.Bounds;
import com.baremaps.osm.domain.Header;

public interface ElementHandler extends EntityHandler {

  @Override
  default void handle(Header header) {
    // Do nothing
  }

  @Override
  default void handle(Bounds bounds) {
    // Do nothing
  }

}
