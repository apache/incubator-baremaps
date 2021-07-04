package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import java.util.function.Consumer;

/**
 * Represents an operation on the entities of changes of different types.
 */
public class ChangeEntityConsumer implements ChangeConsumer {

  private final Consumer<Entity> consumer;

  public ChangeEntityConsumer(Consumer<Entity> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void match(Change change) throws Exception {
    change.getEntities().forEach(consumer);
  }

}
