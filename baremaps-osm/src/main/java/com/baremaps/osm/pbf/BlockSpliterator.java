/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.pbf;

import com.baremaps.osm.stream.StreamException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.Consumer;

public class BlockSpliterator implements Spliterator<Block> {

  private static final int CORES = Runtime.getRuntime().availableProcessors();

  private final ForkJoinPool executor;

  private final BlobIterator iterator;

  private final ArrayBlockingQueue<ForkJoinTask<Block>> queue;

  public BlockSpliterator(InputStream input) {
    executor = Thread.currentThread() instanceof ForkJoinWorkerThread
        ? ((ForkJoinWorkerThread) Thread.currentThread()).getPool()
        : ForkJoinPool.commonPool();
    iterator = new BlobIterator(input);
    queue = new ArrayBlockingQueue<>(CORES * 2);
  }

  @Override
  public Spliterator<Block> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return NONNULL | CONCURRENT | ORDERED;
  }

  @Override
  public boolean tryAdvance(Consumer<? super Block> consumer) {
    ArrayDeque<ForkJoinTask<Block>> batch = new ArrayDeque<>(CORES);
    queue.drainTo(batch);

    for (int i = 0; i < queue.remainingCapacity() && iterator.hasNext(); i++) {
      Blob blob = iterator.next();
      BlockReader reader = new BlockReader(blob);
      try {
        queue.put(executor.submit(() -> reader.readBlock()));
      } catch (InterruptedException e) {
        throw new StreamException(e);
      }
    }

    while (!batch.isEmpty()) {
      ForkJoinTask<Block> future = batch.poll();
      Block entities = future.join();
      consumer.accept(entities);
    }

    if (!iterator.hasNext() && queue.isEmpty()) {
      executor.shutdown();
      return false;
    }

    return true;
  }

}
