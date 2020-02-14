package io.gazetteer.core.stream;

public class StreamException extends RuntimeException {

  /**
   * Constructs a new stream exception with the specified cause.
   *
   * @param cause
   */
  public StreamException(Throwable cause) {
    super(cause);
  }
}
