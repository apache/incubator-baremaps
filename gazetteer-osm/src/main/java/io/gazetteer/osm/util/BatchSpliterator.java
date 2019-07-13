package io.gazetteer.osm.util;

import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Spliterators.spliterator;

public final class BatchSpliterator<T> implements Spliterator<T> {

  private final Spliterator<T> spliterator;

  private final int batchSize;

  public BatchSpliterator(Spliterator<T> spliterator, int batchSize) {
    this.spliterator = spliterator;
    this.batchSize = batchSize;
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> consumer) {
    return spliterator.tryAdvance(consumer);
  }

  @Override
  public Spliterator<T> trySplit() {
    final HoldingConsumer<T> consumer = new HoldingConsumer<>();
    if (!tryAdvance(consumer)) {
      return null;
    }
    final Object[] batch = new Object[batchSize];
    int j = 0;
    do {
      batch[j] = consumer.value;
    } while (++j < batchSize && tryAdvance(consumer));
    return spliterator(batch, 0, j, characteristics() | SIZED);
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return spliterator.characteristics() | SUBSIZED;
  }

}
