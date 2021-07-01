package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;

/**
 * {@inheritDoc}
 */
public interface EntityConsumerAdapter extends EntityConsumer {

  default void match(Header header) throws Exception {
  }

  default void match(Bound bound) throws Exception {
  }

  default void match(Node node) throws Exception {
  }

  default void match(Way way) throws Exception {
  }

  default void match(Relation relation) throws Exception {
  }

}
