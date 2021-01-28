package com.baremaps.util.stream;

/**
 * When a checked exception occurs in a stream, it is a good practice to wrap that exception
 * in an unchecked exception, hence stopping the stream. This exception can then be caught
 * and unwrapped within the block that initiated the stream.
 */
public class StreamException extends RuntimeException {

  /**
   * Creates a new StreamException with the specified cause.
   *
   * @param cause The throwable being wrapped.
   */
  public StreamException(Throwable cause) {
    super(cause);
  }

}
