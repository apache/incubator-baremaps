package io.gazetteer.osm.util;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Spliterators.spliterator;

public class PartitioningSpliterator<T> implements Spliterator<Collection<T>> {

    private final Iterator<T> iterator;
    private final int size;

    public PartitioningSpliterator(Iterator<T> iterator, int partitionSize) {
        assert partitionSize > 0;
        this.iterator = iterator;
        this.size = partitionSize;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Collection<T>> consumer) {
        List<T> partition = new ArrayList<>();
        for (int i = 0; i < size && iterator.hasNext(); i++) {
            partition.add(iterator.next());
        }
        consumer.accept(partition);
        return partition.size() > 0;
    }

    @Override
    public Spliterator<Collection<T>> trySplit() {
        final PartitionConsumer consumer = new PartitionConsumer();
        if (!tryAdvance(consumer)) {
            return null;
        }
        return spliterator(consumer.partition, 0, 1, characteristics() | SIZED);
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | DISTINCT | NONNULL | IMMUTABLE | SUBSIZED;
    }

    static final class PartitionConsumer implements Consumer {

        public final Object[] partition = new Object[1];

        @Override
        public void accept(Object o) {
            partition[0] = partition;
        }
    }

}
