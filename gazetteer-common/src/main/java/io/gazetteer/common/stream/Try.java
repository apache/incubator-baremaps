package io.gazetteer.common.stream;

import java.util.concurrent.Callable;

/**
 * The {@code Try} class represents a computation that may either succeed or fail.
 * @param <T>
 */
public abstract class Try<T> {

  /**
   * The {@code Success} class represents a successful computation.
   * @param <T>
   */
  public static class Success<T> extends Try<T> {

    private final T value;

    /**
     * Construct a {@code Success} with the specified value.
     * @param value
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
   * @param <T>
   */
  public static class Failure<T> extends Try<T> {

    private final Exception exception;

    /**
     * Construct a {@code Failure} with the specified exception.
     * @param exception
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

  /**
   * Returns {@code true} if the {@code Try} is a {@code Success}, {@code false} otherwise.
   * @return
   */
  public abstract boolean isSuccess();

  /**
   * Returns the value of the {@code Try}
   * @return
   */
  public abstract T value();

  /**
   * Returns the exception of the {@code Try}
   * @return
   */
  public abstract Exception exception();

  /**
   * Creates a Try from a Callable
   * @param callable
   * @param <T>
   * @return
   */
  public static <T> Try<T> of(Callable<T> callable) {
    try {
      return new Success(callable.call());
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

}
