package io.gazetteer.osm.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Spliterators.spliterator;

public final class BatchSpliterator<T> implements Spliterator<T> {

  private final Iterator<T> iterator;

  private final int batchSize;

  public BatchSpliterator(Iterator<T> iterator, int batchSize) {
    this.iterator = iterator;
    this.batchSize = batchSize;
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> consumer) {
    if (iterator.hasNext()) {
      consumer.accept(iterator.next());
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void forEachRemaining(Consumer<? super T> consumer) {
    while (iterator.hasNext()) {
      consumer.accept(iterator.next());
    }
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
      batch[j] = consumer.entity;
    } while (++j < batchSize && tryAdvance(consumer));
    return spliterator(batch, 0, j, characteristics() | SIZED);
  }

  @Override
  public Comparator<? super T> getComparator() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return ORDERED | DISTINCT | NONNULL | IMMUTABLE | SUBSIZED;
  }

  static final class HoldingConsumer<T> implements Consumer<T> {

    public Object entity;

    @Override
    public void accept(T value) {
      this.entity = value;
    }
  }
}
