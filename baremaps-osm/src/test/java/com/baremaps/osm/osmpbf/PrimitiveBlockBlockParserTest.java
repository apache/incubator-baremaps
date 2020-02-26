package com.baremaps.osm.osmpbf;

import static com.baremaps.osm.TestUtils.denseOsmPbf;
import static com.baremaps.osm.TestUtils.relationsOsmPbf;
import static com.baremaps.osm.TestUtils.waysOsmPbf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baremaps.core.stream.HoldingConsumer;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;
import com.baremaps.osm.binary.Osmformat;

public class PrimitiveBlockBlockParserTest {

  @Test
  public void readDenseNodes() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = new FileBlockSpliterator(new DataInputStream(denseOsmPbf()));
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlock primitiveBlockReader = new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(consumer.value().getData()));
    List<Node> nodes = primitiveBlockReader.getDenseNodes();
    assertNotNull(nodes);
    assertFalse(nodes.isEmpty());
  }

  @Test
  public void readWays() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = new FileBlockSpliterator(new DataInputStream(waysOsmPbf()));
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlock primitiveBlockReader = new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(consumer.value().getData()));
    List<Way> ways = primitiveBlockReader.getWays();
    assertNotNull(ways);
    assertFalse(ways.isEmpty());
  }

  @Test
  public void readRelations() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = new FileBlockSpliterator(new DataInputStream(relationsOsmPbf()));
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlock primitiveBlockReader = new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(consumer.value().getData()));
    List<Relation> relations = primitiveBlockReader.getRelations();
    assertNotNull(relations);
    assertFalse(relations.isEmpty());
  }
}
