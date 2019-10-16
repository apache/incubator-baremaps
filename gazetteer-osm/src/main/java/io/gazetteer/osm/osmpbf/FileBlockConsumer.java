package io.gazetteer.osm.osmpbf;

import java.util.function.Consumer;

public abstract class FileBlockConsumer implements Consumer<FileBlock> {

  public static final String HEADER = "OSMHeader";
  public static final String DATA = "OSMData";

  @Override
  public void accept(FileBlock block) {
    switch (block.getType()) {
      case HEADER:
        accept( new HeaderBlock(PBFUtil.toHeaderBlock(block)));
        break;
      case DATA:
        accept(new PrimitiveBlock(PBFUtil.toPrimitiveBlock(block)));
        break;
    }
  }

  public abstract void accept(HeaderBlock headerBlock);

  public abstract void accept(PrimitiveBlock data);

}
