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

package com.baremaps.osm.stream;

import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncSpliterator<T, R> implements Spliterator<R> {

  private static final int POOL_SIZE = Math.max(4, Runtime.getRuntime().availableProcessors());

  private static final int QUEUE_SIZE = POOL_SIZE * 2;

  private final Spliterator<T> spliterator;

  private final Function<T, R> operation;

  private final ExecutorService executor;

  private final ArrayBlockingQueue<Future<R>> queue;

  private final Future reader;

  private T value;

  public AsyncSpliterator(Spliterator<T> spliterator, Function<T, R> operation) {
    this.spliterator = spliterator;
    this.operation = operation;
    this.executor = Executors.newFixedThreadPool(POOL_SIZE);
    this.queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    this.reader = executor.submit(() -> {
      while (spliterator.tryAdvance(v -> value = v)) {
        try {
          queue.put(executor.submit(() -> this.operation.apply(value)));
        } catch (InterruptedException e) {
          throw new StreamException(e);
        }
      }
    });
  }

  @Override
  public Spliterator<R> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return spliterator.estimateSize();
  }

  @Override
  public int characteristics() {
    return spliterator.characteristics();
  }

  @Override
  public boolean tryAdvance(Consumer<? super R> consumer) {
    if (reader.isDone() && queue.isEmpty()) {
      executor.shutdown();
      return false;
    }
    ArrayDeque<Future<R>> batch = new ArrayDeque<>();
    queue.drainTo(batch);
    while (!batch.isEmpty()) {
      Future<R> future = batch.poll();
      try {
        consumer.accept(future.get());
      } catch (InterruptedException | ExecutionException e) {
        throw new StreamException(e);
      }
    }
    return true;
  }

}
