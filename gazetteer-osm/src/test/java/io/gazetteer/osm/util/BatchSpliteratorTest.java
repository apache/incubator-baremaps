package io.gazetteer.osm.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import static java.util.Spliterator.*;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

public class BatchSpliteratorTest {

  private BatchSpliterator<Integer> spliterator;

  private int spliteratorSize = 105;
  private int batchSize = 10;

  @Test
  public void tryAdvance() throws Exception {
    for (int i = 0; i < spliteratorSize; i++) {
      assertTrue(spliterator.tryAdvance(block -> {}));
    }
    assertFalse(spliterator.tryAdvance(block -> {}));
  }

  @Test
  public void forEachRemaining() throws Exception {
    Accumulator<Integer> accumulator = new Accumulator<>();
    spliterator.forEachRemaining(accumulator);
    assertEquals(accumulator.acc.size(), spliteratorSize);
  }

  @Test
  public void trySplit() {
    Spliterator<Integer> s;
    for (int i = 0; i < spliteratorSize / batchSize; i++) {
      s = spliterator.trySplit();
      assertNotNull(s);
      assertEquals(s.estimateSize(), batchSize);
    }
    assertNotNull(spliterator);
    assertEquals(spliterator.trySplit().estimateSize(), spliteratorSize % batchSize);
    assertNull(spliterator.trySplit());
  }

  @Test
  public void getComparator() {
    assertNull(spliterator.getComparator());
  }

  @Test
  public void estimateSize() {
    assertEquals(spliterator.estimateSize(), Long.MAX_VALUE);
  }

  @Test
  public void characteristics() {
    assertEquals(
        spliterator.characteristics(), ORDERED | DISTINCT | NONNULL | IMMUTABLE | SUBSIZED);
  }

  @Before
  public void setUp() {
    List<Integer> ints = new ArrayList<>();
    for (int i = 0; i < spliteratorSize; i++) {
      ints.add(i);
    }
    spliterator = new BatchSpliterator<>(ints.iterator(), batchSize);
  }
}
