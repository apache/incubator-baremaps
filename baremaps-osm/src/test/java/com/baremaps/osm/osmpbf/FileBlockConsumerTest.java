package com.baremaps.osm.osmpbf;

import static com.baremaps.osm.TestUtils.dataOsmPbf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

public class FileBlockConsumerTest {

  @Test
  public void accept() {
    HolderFileBlockConsumer consumer = new HolderFileBlockConsumer();
    Spliterator<FileBlock> spliterator = new FileBlockSpliterator(new DataInputStream(dataOsmPbf()));
    spliterator.forEachRemaining(consumer);
    assertEquals(consumer.headerBlocks.size(), 1);
    assertEquals(consumer.primitiveBlocks.size(), 9);
  }

  class HolderFileBlockConsumer extends FileBlockConsumer {

    public List<HeaderBlock> headerBlocks = new ArrayList<>();

    public List<PrimitiveBlock> primitiveBlocks = new ArrayList<>();

    @Override
    public void accept(HeaderBlock headerBlock) {
      headerBlocks.add(headerBlock);
    }

    @Override
    public void accept(PrimitiveBlock primitiveBlock) {
      primitiveBlocks.add(primitiveBlock);
    }
  }
}
