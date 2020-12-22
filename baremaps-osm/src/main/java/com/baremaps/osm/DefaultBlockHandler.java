package com.baremaps.osm;

import com.baremaps.osm.pbf.DataBlock;
import com.baremaps.osm.pbf.HeaderBlock;

public interface DefaultBlockHandler extends BlockHandler {

  default void handle(HeaderBlock headerBlock) throws Exception {
  }

  default void handle(DataBlock dataBlock) throws Exception {
  }
}
