package io.gazetteer.common.stream;

import static java.util.Spliterators.spliterator;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A {@code Spliterator} that creates batches of values for parallel processing.
 * @param <T>
 */
public final class BatchSpliterator<T> implements Spliterator<T> {

  private final Spliterator<T> spliterator;

  private final int batchSize;

  /**
   *
   * @param spliterator
   * @param batchSize
   */
  public BatchSpliterator(Spliterator<T> spliterator, int batchSize) {
    this.spliterator = spliterator;
    this.batchSize = batchSize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean tryAdvance(Consumer<? super T> consumer) {
    return spliterator.tryAdvance(consumer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Spliterator<T> trySplit() {
    final HoldingConsumer<T> consumer = new HoldingConsumer<>();
    if (!tryAdvance(consumer)) {
      return null;
    }
    final Object[] batch = new Object[batchSize];
    int j = 0;
    do {
      batch[j] = consumer.value();
    } while (++j < batchSize && tryAdvance(consumer));
    return spliterator(batch, 0, j, characteristics() | SIZED);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int characteristics() {
    return spliterator.characteristics() | SUBSIZED;
  }

}
