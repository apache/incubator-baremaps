package io.gazetteer.osm.osmpbf;

import java.util.function.Consumer;

public abstract class FileBlockConsumer implements Consumer<FileBlock> {

  public static final String HEADER = "OSMHeader";
  public static final String DATA = "OSMData";

  @Override
  public void accept(FileBlock block) {
    switch (block.getType()) {
      case HEADER:
        accept(new HeaderBlockReader(PBFUtil.toHeaderBlock(block)).readHeader());
        break;
      case DATA:
        accept(new PrimitiveBlockReader(PBFUtil.toPrimitiveBlock(block)).readData());
        break;
    }
  }

  public abstract void accept(Header header);

  public abstract void accept(Data data);

}
