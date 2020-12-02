package com.baremaps.osm.pbf;

import com.baremaps.osm.domain.Entity;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface FileBlock {

  default Stream<Entity> streamEntities() {
    Stream.Builder<Entity> stream = Stream.builder();
    readEntities(stream);
    return stream.build();
  }

  void readEntities(Consumer<Entity> consumer);

}
