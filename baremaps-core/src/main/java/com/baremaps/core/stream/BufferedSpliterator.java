/*
 * Copyright (C) 2020 The Baremaps Authors
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

package com.baremaps.core.stream;

import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

class BufferedSpliterator<T> implements Spliterator<CompletableFuture<T>> {

  private final CompletionStrategy completionStrategy;

  private final Spliterator<CompletableFuture<T>> spliterator;

  private final int bufferSize;

  private final BlockingQueue<CompletableFuture<T>> buffer;

  private int pending = 0;

  public BufferedSpliterator(
      Spliterator<CompletableFuture<T>> spliterator,
      int bufferSize,
      CompletionStrategy completionStrategy) {
    this.spliterator = spliterator;
    this.bufferSize = bufferSize;
    this.buffer = new ArrayBlockingQueue<>(bufferSize);
    this.completionStrategy = completionStrategy;
  }

  @Override
  public boolean tryAdvance(Consumer<? super CompletableFuture<T>> action) {
    fillBuffer();
    if (pending == 0) {
      return false;
    }
    try {
      CompletableFuture<T> future = buffer.take();
      pending--;
      action.accept(future);
      return true;
    } catch (InterruptedException e) {
      throw new StreamException((e));
    }
  }

  @Override
  public Spliterator<CompletableFuture<T>> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    long estimate = pending + spliterator.estimateSize();
    if (estimate < 0) {
      return Long.MAX_VALUE;
    }
    return estimate;
  }

  @Override
  public int characteristics() {
    return spliterator.characteristics();
  }

  private void fillBuffer() {
    while (pending < bufferSize
        && spliterator.tryAdvance(future -> completionStrategy.registerCompletion(future, buffer::add))) {
      pending++;
    }
  }

  public interface CompletionStrategy {

    <T> void registerCompletion(CompletableFuture<T> future, Consumer<CompletableFuture<T>> resultConsumer);
  }

  enum InCompletionOrder implements CompletionStrategy {
    INSTANCE;

    @Override
    public <T> void registerCompletion(CompletableFuture<T> future, Consumer<CompletableFuture<T>> resultConsumer) {
      future.thenAccept(result -> resultConsumer.accept(future));
    }
  }

  enum InSourceOrder implements CompletionStrategy {
    INSTANCE;

    @Override
    public <T> void registerCompletion(CompletableFuture<T> future, Consumer<CompletableFuture<T>> resultConsumer) {
      resultConsumer.accept(future);
    }
  }

}
