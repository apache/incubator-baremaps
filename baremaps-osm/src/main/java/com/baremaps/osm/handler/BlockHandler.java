package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;
import com.baremaps.stream.StreamException;
import java.util.function.Consumer;

public interface BlockHandler extends Consumer<Block> {

  @Override
  default void accept(Block block) {
    try {
      if (block != null) {
        block.handle(this);
      }
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void handle(HeaderBlock headerBlock) throws Exception;

  void handle(DataBlock dataBlock) throws Exception;

}
