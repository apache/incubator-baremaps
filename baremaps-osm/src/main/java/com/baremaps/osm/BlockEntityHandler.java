package com.baremaps.osm;

import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.pbf.DataBlock;
import com.baremaps.osm.pbf.HeaderBlock;
import java.util.function.Consumer;

public class BlockEntityHandler implements BlockHandler {

  private final Consumer<Entity> consumer;

  public BlockEntityHandler(Consumer<Entity> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void handle(HeaderBlock headerBlock) throws Exception {
    consumer.accept(headerBlock.getHeader());
    consumer.accept(headerBlock.getBound());
  }

  @Override
  public void handle(DataBlock dataBlock) throws Exception {
    dataBlock.getDenseNodes().forEach(consumer);
    dataBlock.getNodes().forEach(consumer);
    dataBlock.getWays().forEach(consumer);
    dataBlock.getRelations().forEach(consumer);
  }

}
