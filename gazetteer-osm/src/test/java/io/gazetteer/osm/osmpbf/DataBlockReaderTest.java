package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.io.IOException;
import java.util.List;

import static io.gazetteer.osm.OSMTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataBlockReaderTest {

  @Test
  public void read() throws IOException {
    FileBlockIterator fileBlockIterator = (FileBlockIterator) PBFUtil.iterator(osmPbfData());
    FileBlock headerBlock = fileBlockIterator.next();
    DataBlockReader primitiveBlockReader =
        new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(headerBlock.getData()));
    DataBlock primitiveBlock = primitiveBlockReader.read();
    assertNotNull(primitiveBlock);
    for (int i = 0; i < 9; i++) {
      primitiveBlock = primitiveBlockReader.read();
      assertNotNull(primitiveBlock);
    }
  }

  @Test
  public void readDenseNodes() throws IOException {
    FileBlockIterator reader = (FileBlockIterator) PBFUtil.iterator(osmPbfDenseBlock());
    FileBlock block = reader.next();
    DataBlockReader primitiveBlockReader =
        new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(block.getData()));
    List<Node> nodes = primitiveBlockReader.readDenseNodes();
    assertNotNull(nodes);
    assertFalse(nodes.isEmpty());
  }

  @Test
  public void readWays() throws IOException {
    FileBlockIterator reader = (FileBlockIterator) PBFUtil.iterator(osmPbfWaysBlock());
    FileBlock block = reader.next();
    DataBlockReader primitiveBlockReader =
        new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(block.getData()));
    List<Way> ways = primitiveBlockReader.readWays();
    assertNotNull(ways);
    assertFalse(ways.isEmpty());
  }

  @Test
  public void readRelations() throws IOException {
    FileBlockIterator reader = (FileBlockIterator) PBFUtil.iterator(osmPbfRelationsBlock());
    FileBlock block = reader.next();
    DataBlockReader primitiveBlockReader =
        new DataBlockReader(Osmformat.PrimitiveBlock.parseFrom(block.getData()));
    List<Relation> relations = primitiveBlockReader.readRelations();
    assertNotNull(relations);
    assertFalse(relations.isEmpty());
  }
}
