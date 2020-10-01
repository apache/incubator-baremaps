package com.baremaps.util.stream;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProgressPeeker<T> implements Consumer<T> {

  private static Logger logger = LogManager.getLogger();

  private final long size;

  private final AtomicLong position;

  private final long start;

  private final AtomicLong timestamp;

  public ProgressPeeker(long size) {
    this(size, e -> 1l);

  }

  public ProgressPeeker(long size, Function<T, Long> increment) {
    this.size = size;
    this.position = new AtomicLong(0);
    this.start = System.currentTimeMillis();
    this.timestamp = new AtomicLong(System.currentTimeMillis());
  }

  @Override
  public void accept(T e) {
    long p = position.incrementAndGet();
    long t = System.currentTimeMillis();
    long l = timestamp.get();
    double duration = (t - start) / 1000d;
    if (t - l >= 1000) {
      timestamp.set(t);
      double progress = Math.round(p * 10000d / size) / 100d;
      double eta = (duration / progress * 100) - duration;
      logger.info("progress: {}%, duration: {}s, ETA: {}s", progress, Math.round(duration), Math.round(eta));
    }
    if (p == size) {
      double progress = 100d;
      double eta = (duration / progress * 100) - duration;
      logger.info("progress: {}%, duration: {}s, ETA: {}s", progress, Math.round(duration), Math.round(eta));
    }
  }

}
