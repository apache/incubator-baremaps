package com.baremaps.osm;

import com.baremaps.osm.model.Bounds;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.stream.StreamException;
import java.util.function.Consumer;

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
