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

import com.baremaps.osm.domain.Entity;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ForkJoinBlobSpliterator implements Spliterator<Stream<Entity>> {

  private final ForkJoinPool pool;

  private final BlobIterator iterator;

  private final ArrayBlockingQueue<ForkJoinTask<Stream<Entity>>> queue;

  private final Future reader;

  public ForkJoinBlobSpliterator(InputStream input) {
    if (Thread.currentThread() instanceof ForkJoinWorkerThread) {
      pool = ((ForkJoinWorkerThread) Thread.currentThread()).getPool();
    } else {
      pool = ForkJoinPool.commonPool();
    }
    iterator = new BlobIterator(input);
    queue = new ArrayBlockingQueue<>(Runtime.getRuntime().availableProcessors());
    reader = pool.submit(() -> {
      while (iterator.hasNext()) {
        Blob blob = iterator.next();
        BlobReader reader = new BlobReader(blob);
        try {
          queue.put(pool.submit(() -> reader.read()));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public Spliterator<Stream<Entity>> trySplit() {
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
  public boolean tryAdvance(Consumer<? super Stream<Entity>> consumer) {
    if (reader.isDone() && queue.isEmpty()) {
      return false;
    }
    ArrayDeque<ForkJoinTask<Stream<Entity>>> batch = new ArrayDeque<>();
    queue.drainTo(batch);
    while (!batch.isEmpty()) {
      ForkJoinTask<Stream<Entity>> stream = batch.poll();
      consumer.accept(stream.join());
    }
    return true;
  }

}
