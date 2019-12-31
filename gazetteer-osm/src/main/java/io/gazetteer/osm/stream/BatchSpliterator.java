package io.gazetteer.osm.stream;

import java.util.Spliterator;
import java.util.Spliterators;

public abstract class BatchSpliterator<T> implements Spliterator<T> {

  protected final int batchSize;
  protected final int characteristics;

  public BatchSpliterator(int batchSize, int characteristics) {
    this.batchSize = batchSize;
    this.characteristics = characteristics;
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
    return characteristics;
  }
}
