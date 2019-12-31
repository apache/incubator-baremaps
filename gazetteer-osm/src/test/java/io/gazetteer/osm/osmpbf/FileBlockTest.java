package io.gazetteer.osm.osmpbf;

import static io.gazetteer.osm.TestUtils.dataOsmPbf;
import static io.gazetteer.osm.TestUtils.invalidOsmPbf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gazetteer.osm.stream.StreamException;
import java.io.DataInputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

public class FileBlockTest {

  @Test
  public void stream() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .count()
        == 10);
  }

  @Test
  public void isHeaderBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isHeaderBlock)
        .count()
        == 1);
  }

  @Test
  public void isDataBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isPrimitiveBlock)
        .count()
        == 9);
  }

  @Test
  public void toHeaderBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isHeaderBlock)
        .map(FileBlock::toHeaderBlock)
        .count()
        == 1);
  }

  @Test
  public void toDataBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isPrimitiveBlock)
        .map(FileBlock::toPrimitiveBlock)
        .collect(Collectors.toList())
        .size()
        == 9);
  }

  @Test
  public void toDataBlockException() {
    assertThrows(StreamException.class, () -> {
      invalidOsmPbf().toPrimitiveBlock();
    });
  }

}
