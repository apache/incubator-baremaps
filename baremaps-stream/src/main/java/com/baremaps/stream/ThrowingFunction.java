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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

  R apply(T arg) throws E;

  static <T, R> Function<T, Optional<R>> lifted(
      final ThrowingFunction<? super T, ? extends R, ?> function) {
    requireNonNull(function);

    return t -> {
      try {
        return Optional.ofNullable(function.apply(t));
      } catch (final Exception e) {
        return Optional.empty();
      }
    };
  }

  static <T, R> Function<T, R> unchecked(
      final ThrowingFunction<? super T, ? extends R, ?> function) {
    requireNonNull(function);
    return t -> {
      try {
        return function.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
