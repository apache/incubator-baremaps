package com.baremaps.core;


/** Signals that an exception occurred during the execution of a {@code Pipeline}. */
public class PipelineException extends RuntimeException {

  /** Constructs a {@code PipelineException} with {@code null} as its error detail message. */
  public PipelineException() {}

  /**
   * Constructs an {@code PipelineException} with the specified detail message.
   *
   * @param message the message
   */
  public PipelineException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code PipelineException} with the specified cause.
   *
   * @param cause the cause
   */
  public PipelineException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code PipelineException} with the specified detail message and cause.
   *
   * @param message the message
   * @param cause the cause
   */
  public PipelineException(String message, Throwable cause) {
    super(message, cause);
  }
}
