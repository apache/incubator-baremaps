package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Entity;
import com.baremaps.stream.StreamException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface PartitionHandler extends Consumer<Stream<Entity>> {

  @Override
  default void accept(Stream<Entity> entities) {
    try {
      handle(entities);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void handle(Stream<Entity> entities) throws Exception;

}
