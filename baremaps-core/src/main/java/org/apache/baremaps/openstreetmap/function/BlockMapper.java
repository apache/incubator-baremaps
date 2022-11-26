package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.openstreetmap.model.HeaderBlock;

import java.util.function.Function;

/** A function that transforms a block. */
public record BlockMapper(
  Function<HeaderBlock, HeaderBlock> headerBlockMapper,
  Function<DataBlock, DataBlock> dataBlockMapper
) implements Function<Block, Block> {

  /** {@inheritDoc} */
  @Override
  public Block apply(Block block) {
    if (block instanceof HeaderBlock headerBlock) {
      return headerBlockMapper.apply(headerBlock);
    } else if (block instanceof DataBlock dataBlock) {
      return dataBlockMapper.apply(dataBlock);
    } else {
      throw new IllegalArgumentException("Unknown block type.");
    }
  }
}
