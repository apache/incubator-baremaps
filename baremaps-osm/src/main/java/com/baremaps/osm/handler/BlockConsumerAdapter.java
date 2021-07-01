package com.baremaps.osm.handler;

import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;

/**
 * {@inheritDoc}
 */
public interface BlockConsumerAdapter extends BlockConsumer {

  default void match(HeaderBlock headerBlock) throws Exception {
  }

  default void match(DataBlock dataBlock) throws Exception {
  }
}
