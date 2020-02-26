package com.baremaps.osm.osmpbf;

import java.util.function.Consumer;

public abstract class FileBlockConsumer implements Consumer<FileBlock> {

  @Override
  public void accept(FileBlock block) {
    switch (block.getType()) {
      case OSMHeader:
        accept(block.toHeaderBlock());
        break;
      case OSMData:
        accept(block.toPrimitiveBlock());
        break;
      default:
        break;
    }
  }

  public abstract void accept(HeaderBlock headerBlock);

  public abstract void accept(PrimitiveBlock primitiveBlock);
}
