package io.gazetteer.osm.stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class BatchSpliterator<T> implements Spliterator<T> {

  private final Spliterator<T> spliterator;
  private final int batchSize;

  public BatchSpliterator(Spliterator<T> spliterator, int batchSize) {
    this.spliterator = spliterator;
    this.batchSize = batchSize;
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    return this.spliterator.tryAdvance(action);
  }

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

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return spliterator.characteristics() | SUBSIZED;
  }
}
