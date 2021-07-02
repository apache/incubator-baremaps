package com.baremaps.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PartitionedSpliterator<T> implements Spliterator<Stream<T>> {

  private final Spliterator<T> spliterator;

  private final int partitionSize;

  public PartitionedSpliterator(Spliterator<T> spliterator, int partitionSize) {
    this.spliterator = spliterator;
    this.partitionSize = partitionSize;
  }

  @Override
  public boolean tryAdvance(Consumer<? super Stream<T>> action) {
    Stream.Builder<T> partition = Stream.builder();
    int size = 0;
    while (size < partitionSize && spliterator.tryAdvance(partition::add)) {
      size++;
    }
    if (size == 0) {
      return false;
    }
    action.accept(partition.build());
    return true;
  }

  @Override
  public Spliterator<Stream<T>> trySplit() {
    HoldingConsumer<Stream<T>> consumer = new HoldingConsumer<>();
    tryAdvance(consumer);
    return Stream.ofNullable(consumer.value()).spliterator();
  }

  @Override
  public long estimateSize() {
    return spliterator.estimateSize() / partitionSize;
  }

  @Override
  public int characteristics() {
    return spliterator.characteristics();
  }

}
