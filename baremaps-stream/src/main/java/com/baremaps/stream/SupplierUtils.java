package com.baremaps.stream;

import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierUtils {

  public static <T> Supplier<T> memoize(Supplier<T> supplier) {
    T value = supplier.get();
    return () -> value;
  }

  public static <T> Supplier<T> memoize(Supplier<T> supplier, int ttlMs) {
    return new Supplier() {
      long t1 = System.currentTimeMillis();
      T value = supplier.get();

      @Override
      public Object get() {
        long t2 = System.currentTimeMillis();
        if (t2 - t1 > ttlMs) {
          t1 = t2;
          value = supplier.get();
        }
        return value;
      }
    };
  }

  public static <T, R> Supplier<R> convert(Supplier<T> supplier, Function<T, R> function) {
    return () -> function.apply(supplier.get());
  }

}
