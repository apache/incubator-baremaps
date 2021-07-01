package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;

/**
 * Represents an operation on elements of different types.
 */
public interface ElementConsumer extends EntityConsumer {

  @Override
  default void match(Header header) {
    // Do nothing
  }

  @Override
  default void match(Bound bound) {
    // Do nothing
  }

}
