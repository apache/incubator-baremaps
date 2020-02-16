package io.gazetteer.core.stream;

public class StreamException extends RuntimeException {

  /**
   * Creates a new stream exception with the specified cause. When a checked exception occurs in a stream, it
   * is a good practice to wrap that exception in an unchecked exception, hence stopping the stream. This
   * exception can then be catched and unwrapped within the block that initiated the stream.
   *
   * @param cause The throwable being wrapped.
   */
  public StreamException(Throwable cause) {
    super(cause);
  }

}
