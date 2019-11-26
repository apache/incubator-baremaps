package io.gazetteer.osm.osmpbf;

import static io.gazetteer.osm.OSMTestUtil.osmPbfDenseBlock;
import static io.gazetteer.osm.OSMTestUtil.osmPbfRelationsBlock;
import static io.gazetteer.osm.OSMTestUtil.osmPbfWaysBlock;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.gazetteer.common.stream.HoldingConsumer;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

public class PrimitiveBlockBlockParserTest {

  @Test
  public void readDenseNodes() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = PBFUtil.spliterator(osmPbfDenseBlock());
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlock primitiveBlockReader = new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(consumer.value().getData()));
    List<Node> nodes = primitiveBlockReader.getDenseNodes();
    assertNotNull(nodes);
    assertFalse(nodes.isEmpty());
  }

  @Test
  public void readWays() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = PBFUtil.spliterator(osmPbfWaysBlock());
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlock primitiveBlockReader = new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(consumer.value().getData()));
    List<Way> ways = primitiveBlockReader.getWays();
    assertNotNull(ways);
    assertFalse(ways.isEmpty());
  }

  @Test
  public void readRelations() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = PBFUtil.spliterator(osmPbfRelationsBlock());
    fileBlockIterator.tryAdvance(consumer);
    PrimitiveBlock primitiveBlockReader = new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(consumer.value().getData()));
    List<Relation> relations = primitiveBlockReader.getRelations();
    assertNotNull(relations);
    assertFalse(relations.isEmpty());
  }

}
