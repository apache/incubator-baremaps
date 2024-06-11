package org.apache.baremaps.cli;

import org.apache.baremaps.data.storage.DataStore;
import org.apache.baremaps.data.storage.DataStoreException;

/** Signals that an exception occurred in the {@link Baremaps} CLI. */
public class BaremapsException extends RuntimeException {

    /** Constructs a {@link BaremapsException} with {@code null} as its error detail message. */
    public BaremapsException() {}

    /**
     * Constructs an {@link BaremapsException} with the specified detail message.
     *
     * @param message the message
     */
    public BaremapsException(String message) {
        super(message);
    }

    /**
     * Constructs a {@link BaremapsException} with the specified cause.
     *
     * @param cause the cause
     */
    public BaremapsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a {@link BaremapsException} with the specified detail message and cause.
     *
     * @param message the message
     * @param cause the cause
     */
    public BaremapsException(String message, Throwable cause) {
        super(message, cause);
    }
}
