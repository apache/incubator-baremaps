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
import com.baremaps.osm.stream.StreamException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class AsyncBlobSpliterator implements Spliterator<Stream<Entity>> {

  private static final int POOL_SIZE = Math.max(4, Runtime.getRuntime().availableProcessors());

  private static final int QUEUE_SIZE = POOL_SIZE * 2;

  private final ExecutorService executor;

  private final BlobIterator iterator;

  private final ArrayBlockingQueue<Future<Stream<Entity>>> queue;

  private final Future reader;

  public AsyncBlobSpliterator(InputStream input) {
    executor = Executors.newFixedThreadPool(POOL_SIZE);
    iterator = new BlobIterator(input);
    queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    reader = executor.submit(() -> {
      while (iterator.hasNext()) {
        Blob blob = iterator.next();
        BlobReader reader = new BlobReader(blob);
        try {
          queue.put(executor.submit(() -> reader.read()));
        } catch (InterruptedException e) {
          throw new StreamException(e);
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
      executor.shutdown();
      return false;
    }
    ArrayDeque<Future<Stream<Entity>>> batch = new ArrayDeque<>();
    queue.drainTo(batch);
    while (!batch.isEmpty()) {
      Future<Stream<Entity>> future = batch.poll();
      try {
        consumer.accept(future.get());
      } catch (InterruptedException | ExecutionException e) {
        throw new StreamException(e);
      }
    }
    return true;
  }

}
