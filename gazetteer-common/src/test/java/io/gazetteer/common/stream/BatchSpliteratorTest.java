package io.gazetteer.common.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BatchSpliteratorTest {

  private BatchSpliterator<Integer> spliterator;

  private int spliteratorSize = 105;
  private int batchSize = 10;

  @Test
  public void tryAdvance() throws Exception {
    for (int i = 0; i < spliteratorSize; i++) {
      Assertions.assertTrue(spliterator.tryAdvance(block -> {}));
    }
    Assertions.assertFalse(spliterator.tryAdvance(block -> {}));
  }

  @Test
  public void forEachRemaining() throws Exception {
    AccumulatingConsumer<Integer> accumulator = new AccumulatingConsumer<>();
    spliterator.forEachRemaining(accumulator);
    Assertions.assertEquals(accumulator.values().size(), spliteratorSize);
  }

  @Test
  public void trySplit() {
    Spliterator<Integer> s;
    for (int i = 0; i < spliteratorSize / batchSize; i++) {
      s = spliterator.trySplit();
      Assertions.assertNotNull(s);
      Assertions.assertEquals(s.estimateSize(), batchSize);
    }
    Assertions.assertNotNull(spliterator);
    Assertions.assertEquals(spliterator.trySplit().estimateSize(), spliteratorSize % batchSize);
    Assertions.assertNull(spliterator.trySplit());
  }

  @Test
  public void estimateSize() {
    Assertions.assertEquals(spliterator.estimateSize(), Long.MAX_VALUE);
  }

  @BeforeEach
  public void setUp() {
    List<Integer> ints = new ArrayList<>();
    for (int i = 0; i < spliteratorSize; i++) {
      ints.add(i);
    }
    spliterator = new BatchSpliterator<>(ints.spliterator(), batchSize);
  }
}
