package com.baremaps.stream;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConsumerUtils {

  private ConsumerUtils() {

  }

  public static <T> Function<T, T> consumeThenReturn(Consumer<T> consumer) {
    return t -> {
      consumer.accept(t);
      return t;
    };
  }

}
