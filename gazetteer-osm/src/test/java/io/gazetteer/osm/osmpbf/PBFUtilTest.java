package io.gazetteer.osm.osmpbf;

import static io.gazetteer.osm.OSMTestUtil.osmPbfData;
import static io.gazetteer.osm.OSMTestUtil.osmPbfInvalidBlock;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gazetteer.common.stream.AccumulatingConsumer;
import io.gazetteer.common.stream.StreamException;
import java.util.Spliterator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class PBFUtilTest {

  @Test
  public void stream() {
    assertTrue(PBFUtil.stream(osmPbfData()).count() == 10);
  }

  @Test
  public void isHeaderBlock() {
    assertTrue(PBFUtil.stream(osmPbfData()).filter(PBFUtil::isHeaderBlock).count() == 1);
  }

  @Test
  public void isDataBlock() {
    assertTrue(PBFUtil.stream(osmPbfData()).filter(PBFUtil::isPrimitiveBlock).count() == 9);
  }

  @Test
  public void toHeaderBlock() {
    assertTrue(
        PBFUtil.stream(osmPbfData())
            .filter(PBFUtil::isHeaderBlock)
            .map(PBFUtil::toHeaderBlock)
            .count()
            == 1);
  }

  @Test
  public void toDataBlock() {
    assertTrue(
        PBFUtil.stream(osmPbfData())
            .filter(PBFUtil::isPrimitiveBlock)
            .map(PBFUtil::toPrimitiveBlock)
            .collect(Collectors.toList())
            .size()
            == 9);
  }

  @Test
  public void toDataBlockException() {
    assertThrows(StreamException.class, () -> {
      PBFUtil.toPrimitiveBlock(osmPbfInvalidBlock());
    });
  }

  @Test
  public void tryAdvance() {
    Spliterator<FileBlock> spliterator = PBFUtil.spliterator(osmPbfData());
    for (int i = 0; i < 10; i++) {
      assertTrue(spliterator.tryAdvance(block -> {
      }));
    }
    assertFalse(spliterator.tryAdvance(block -> {
    }));
  }

  @Test
  public void forEachRemaining() {
    Spliterator<FileBlock> spliterator = PBFUtil.spliterator(osmPbfData());
    AccumulatingConsumer<FileBlock> accumulator = new AccumulatingConsumer<>();
    spliterator.forEachRemaining(accumulator);
    assertTrue(accumulator.values().size() == 10);
  }
}
