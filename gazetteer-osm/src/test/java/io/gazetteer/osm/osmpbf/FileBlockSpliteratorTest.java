package io.gazetteer.osm.osmpbf;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.gazetteer.common.stream.HoldingConsumer;
import java.io.FileNotFoundException;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

public class FileBlockSpliteratorTest {

  @Test
  public void tryAdvance() throws FileNotFoundException {
    Spliterator<FileBlock> reader = PBFUtil.spliterator(osmPbfData());
    reader.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
    assertFalse(reader.tryAdvance(new HoldingConsumer<>()));
  }

}
