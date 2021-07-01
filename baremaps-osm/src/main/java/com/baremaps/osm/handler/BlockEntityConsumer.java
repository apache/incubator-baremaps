package com.baremaps.osm.handler;

import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.HeaderBlock;
import java.util.function.Consumer;

/**
 * Represents an operation on the entities of blocks of different types.
 */
public class BlockEntityConsumer implements BlockConsumer {

  private final Consumer<Entity> consumer;

  public BlockEntityConsumer(Consumer<Entity> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void match(HeaderBlock headerBlock) throws Exception {
    consumer.accept(headerBlock.getHeader());
    consumer.accept(headerBlock.getBound());
  }

  @Override
  public void match(DataBlock dataBlock) throws Exception {
    dataBlock.getDenseNodes().forEach(consumer);
    dataBlock.getNodes().forEach(consumer);
    dataBlock.getWays().forEach(consumer);
    dataBlock.getRelations().forEach(consumer);
  }

}
