package io.gazetteer.osm.osmpbf;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static io.gazetteer.osm.OSMTestUtil.osmPbfDenseBlock;
import static io.gazetteer.osm.OSMTestUtil.osmPbfRelationsBlock;
import static io.gazetteer.osm.OSMTestUtil.osmPbfWaysBlock;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.util.HoldingConsumer;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

public class PrimitiveBlockBlockParserTest {

  @Test
  public void read() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    FileBlockSpliterator fileBlockIterator = (FileBlockSpliterator) PBFUtil.spliterator(osmPbfData());
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlockReader primitiveBlockReader =
        new PrimitiveBlockReader(Osmformat.PrimitiveBlock.parseFrom(consumer.value.getData()));
    PrimitiveBlock primitiveBlock = primitiveBlockReader.readPrimitiveBlock();
    assertNotNull(primitiveBlock);
    for (int i = 0; i < 9; i++) {
      primitiveBlock = primitiveBlockReader.readPrimitiveBlock();
      assertNotNull(primitiveBlock);
    }
  }

  @Test
  public void readDenseNodes() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    FileBlockSpliterator fileBlockIterator = (FileBlockSpliterator) PBFUtil.spliterator(osmPbfDenseBlock());
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlockReader primitiveBlockReader =
        new PrimitiveBlockReader(Osmformat.PrimitiveBlock.parseFrom(consumer.value.getData()));
    List<Node> nodes = primitiveBlockReader.readDenseNodes();
    assertNotNull(nodes);
    assertFalse(nodes.isEmpty());
  }

  @Test
  public void readWays() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    FileBlockSpliterator fileBlockIterator = (FileBlockSpliterator) PBFUtil.spliterator(osmPbfWaysBlock());
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlockReader primitiveBlockReader =
        new PrimitiveBlockReader(Osmformat.PrimitiveBlock.parseFrom(consumer.value.getData()));
    List<Way> ways = primitiveBlockReader.readWays();
    assertNotNull(ways);
    assertFalse(ways.isEmpty());
  }

  @Test
  public void readRelations() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    FileBlockSpliterator fileBlockIterator = (FileBlockSpliterator) PBFUtil.spliterator(osmPbfRelationsBlock());
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlockReader primitiveBlockReader =
        new PrimitiveBlockReader(Osmformat.PrimitiveBlock.parseFrom(consumer.value.getData()));
    List<Relation> relations = primitiveBlockReader.readRelations();
    assertNotNull(relations);
    assertFalse(relations.isEmpty());
  }

}
