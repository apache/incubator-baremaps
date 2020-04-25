/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.util.stream;

import java.util.concurrent.Callable;

/**
 * The {@code Try} class represents a computation that may either succeed or fail. This abstraction can be
 * used to process the elements of a stream that may fail independently from each others.
 *
 * @param <T>
 */
public abstract class Try<T> {

  /**
   * Creates a Try from a Callable
   *
   * @param callable the callable to be executed.
   * @param <T> the return type of the callable.
   * @return a success or a failure.
   */
  public static <T> Try<T> of(Callable<T> callable) {
    try {
      return new Success(callable.call());
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

  /**
   * Returns {@code true} if the {@code Try} is a {@code Success}, {@code false} otherwise.
   *
   * @return {@code true} if the {@code Try} is a {@code Success}, {@code false} otherwise.
   */
  public abstract boolean isSuccess();

  /**
   * Returns the value of the {@code Try}.
   *
   * @return the value of the {@code Try}.
   */
  public abstract T value();

  /**
   * Returns the exception of the {@code Try}.
   *
   * @return the exception of the {@code Try}.
   */
  public abstract Exception exception();

  /**
   * The {@code Success} class represents a successful computation.
   *
   * @param <T>
   */
  public static class Success<T> extends Try<T> {

    private final T value;

    /**
     * Construct a {@code Success} with the specified value.
     *
     * @param value the values.
     */
    public Success(T value) {
      this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSuccess() {
      return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T value() {
      return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Exception exception() {
      return null;
    }
  }

  /**
   * The {@code Failure} class represents a failed computation.
   *
   * @param <T>
   */
  public static class Failure<T> extends Try<T> {

    private final Exception exception;

    /**
     * Construct a {@code Failure} with the specified exception.
     *
     * @param exception the exception.
     */
    public Failure(Exception exception) {
      this.exception = exception;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSuccess() {
      return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T value() {
      return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Exception exception() {
      return exception;
    }
  }
}
