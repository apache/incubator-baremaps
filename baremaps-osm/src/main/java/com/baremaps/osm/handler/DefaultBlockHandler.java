package com.baremaps.osm.handler;

import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;

public interface DefaultBlockHandler extends BlockHandler {

  default void handle(HeaderBlock headerBlock) throws Exception {
  }

  default void handle(DataBlock dataBlock) throws Exception {
  }
}
