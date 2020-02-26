package com.baremaps.core.stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * A {@code BatchSpliterator} wraps another spliterator and partition its elements according to a given batch size
 * when trySplit is invoked.
 *
 * @param <T>
 */
public class BatchSpliterator<T> implements Spliterator<T> {

  private final Spliterator<T> spliterator;
  private final int batchSize;

  /**
   * Creates a spliterator that partitions the underlying spliterator according to the given batch size.
   *
   * @param spliterator the underlying spliterator.
   * @param batchSize   the batch size.
   */
  public BatchSpliterator(Spliterator<T> spliterator, int batchSize) {
    this.spliterator = spliterator;
    this.batchSize = batchSize;
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    return this.spliterator.tryAdvance(action);
  }

  /**
   * Returns a spliterator covering the elements of a batch.
   *
   * @return a spliterator covering the elements of a batch.
   */
  @Override
  public Spliterator<T> trySplit() {
    HoldingConsumer<T> holder = new HoldingConsumer<>();
    if (tryAdvance(holder)) {
      Object[] a = new Object[batchSize];
      int j = 0;
      do {
        a[j] = holder.value();
      } while (++j < batchSize && tryAdvance(holder));
      return Spliterators.spliterator(a, 0, j, characteristics());
    }
    return null;
  }

  /**
   * Returns {@code Long.MAX_VALUE} assuming that the underlying spliterator is of unknown size.
   *
   * @return {@code Long.MAX_VALUE} corresponding to unknown size.
   */
  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  /**
   * Returns the characteristics of the underlying spliterator with its ability to be subsized.
   *
   * @return a representation of characteristics.
   */
  @Override
  public int characteristics() {
    return spliterator.characteristics() | SUBSIZED;
  }

}
