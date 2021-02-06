package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;

/**
 * A class that uses the visitor pattern to dispatch operations on entities.
 */
public interface DefaultEntityHandler extends EntityHandler {

  default void handle(Header header) throws Exception {
  }

  default void handle(Bound bound) throws Exception {
  }

  default void handle(Node node) throws Exception {
  }

  default void handle(Way way) throws Exception {
  }

  default void handle(Relation relation) throws Exception {
  }

}
