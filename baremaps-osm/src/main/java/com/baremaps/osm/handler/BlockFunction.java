package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;
import com.baremaps.stream.StreamException;
import java.util.function.Function;

public interface BlockFunction <T> extends Function<Block, T> {

  @Override
  default T apply(Block block) {
    try {
      return block.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  T match(HeaderBlock headerBlock) throws Exception;

  T match(DataBlock dataBlock) throws Exception;

}