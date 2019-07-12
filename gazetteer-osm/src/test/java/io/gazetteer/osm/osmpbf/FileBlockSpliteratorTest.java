package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.util.HoldingConsumer;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static org.junit.jupiter.api.Assertions.*;

public class FileBlockSpliteratorTest {

  @Test
  public void tryAdvance() throws FileNotFoundException {
    Spliterator<FileBlock> reader = PBFUtil.spliterator(osmPbfData());
    reader.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
    assertFalse(reader.tryAdvance(new HoldingConsumer<>()));
  }

}
