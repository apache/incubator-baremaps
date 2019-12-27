package io.gazetteer.osm.osmpbf;

import static io.gazetteer.osm.TestConstants.dataOsmPbf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gazetteer.osm.stream.AccumulatingConsumer;
import io.gazetteer.osm.stream.HoldingConsumer;
import java.io.DataInputStream;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

public class FileBlockSpliteratorTest {

  @Test
  public void tryAdvance() {
    Spliterator<FileBlock> spliterator = new FileBlockSpliterator(new DataInputStream(dataOsmPbf()));
    spliterator.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
    assertFalse(spliterator.tryAdvance(new HoldingConsumer<>()));
  }

  @Test
  public void forEachRemaining() {
    Spliterator<FileBlock> spliterator = new FileBlockSpliterator(new DataInputStream(dataOsmPbf()));
    AccumulatingConsumer<FileBlock> accumulator = new AccumulatingConsumer<>();
    spliterator.forEachRemaining(accumulator);
    assertTrue(accumulator.values().size() == 10);
  }
}
