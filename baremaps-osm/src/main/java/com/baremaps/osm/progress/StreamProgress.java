package com.baremaps.osm.progress;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class StreamProgress<T> implements Consumer<T> {

  private final AtomicLong position = new AtomicLong(0);

  private final Consumer<Long> listener;

  public StreamProgress(Long size, Integer tick) {
    this(new ProgressLogger(size, tick));
  }

  public StreamProgress(Consumer<Long> listener) {
    this.listener = listener;
  }

  @Override
  public void accept(T e) {
    listener.accept(position.incrementAndGet());
  }

}
