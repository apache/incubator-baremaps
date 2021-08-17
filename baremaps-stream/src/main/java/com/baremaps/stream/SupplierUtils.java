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
