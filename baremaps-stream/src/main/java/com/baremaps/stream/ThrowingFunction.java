package com.baremaps.stream;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

  R apply(T arg) throws E;

  static <T, R> Function<T, Optional<R>> lifted(final ThrowingFunction<? super T, ? extends R, ?> function) {
    requireNonNull(function);

    return t -> {
      try {
        return Optional.ofNullable(function.apply(t));
      } catch (final Exception e) {
        return Optional.empty();
      }
    };
  }

  static <T, R> Function<T, R> unchecked(final ThrowingFunction<? super T, ? extends R, ?> function) {
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
