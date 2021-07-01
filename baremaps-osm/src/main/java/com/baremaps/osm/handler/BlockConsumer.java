package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;
import com.baremaps.stream.StreamException;
import java.util.function.Consumer;

/**
 * Represents an operation on blocks of different types.
 */
public interface BlockConsumer extends Consumer<Block> {

  @Override
  default void accept(Block block) {
    try {
      block.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void match(HeaderBlock headerBlock) throws Exception;

  void match(DataBlock dataBlock) throws Exception;

}
