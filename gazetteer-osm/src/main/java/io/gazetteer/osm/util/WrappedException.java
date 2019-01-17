package io.gazetteer.osm.util;

public class WrappedException extends RuntimeException {

    private final Throwable cause;

    public WrappedException(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

}
