package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Entity;
import java.util.stream.Stream;

public interface PartitionHandlerAdapter {

  default void handle(Stream<Entity> entities) throws Exception {

  }

}
