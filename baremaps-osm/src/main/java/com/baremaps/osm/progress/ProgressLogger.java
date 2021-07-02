package com.baremaps.osm.progress;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressLogger implements Consumer<Long> {

  private static final Logger logger = LoggerFactory.getLogger(ProgressLogger.class);

  private final long size;

  private final int tick;

  // A volatile does not guarantee atomicity but blocking with an AtomicLong is not worth it.
  private volatile long timestamp;

  public ProgressLogger(long size, int tick) {
    this.size = size;
    this.tick = tick;
    this.timestamp = System.currentTimeMillis();
  }

  @Override
  public void accept(Long position) {
    long t = System.currentTimeMillis();
    long l = timestamp;
    if (size >= 0 && t - l >= tick) {
      timestamp = t;
      double progress = Math.round(position * 10000d / size) / 100d;
      logger.info("{}%", progress);
    }
    if (size >= 0 && position == size) {
      double progress = 100d;
      logger.info("{}%", progress);
    }
  }
}
