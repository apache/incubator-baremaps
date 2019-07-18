package io.gazetteer.common.stream;

import java.util.concurrent.Callable;

public abstract class Try<T> {

  public static class Success<T> extends Try<T> {

    private final T value;

    public Success(T value) {
      this.value = value;
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public T value() {
      return value;
    }

    @Override
    public Exception exception() {
      return null;
    }
  }

  public static class Failure<T> extends Try<T> {

    private final Exception exception;

    public Failure(Exception exception) {
      this.exception = exception;
    }

    @Override
    public boolean isSuccess() {
      return false;
    }

    @Override
    public T value() {
      return null;
    }

    @Override
    public Exception exception() {
      return exception;
    }
  }

  public abstract boolean isSuccess();

  public abstract T value();

  public abstract Exception exception();

  public static <T> Try<T> of(Callable<T> callable) {
    try {
      return new Success(callable.call());
    } catch (Exception e) {
      return new Failure<>(e);
    }
  }

}
