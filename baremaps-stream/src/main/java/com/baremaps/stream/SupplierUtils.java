package com.baremaps.stream;

import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierUtils {

  public static <T, R> Supplier<R> convert(Supplier<T> supplier, Function<T, R> function) {
    return () -> function.apply(supplier.get());
  }

}
