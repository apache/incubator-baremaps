package io.gazetteer.osm.util;

public class StreamException extends RuntimeException {

    private final Throwable cause;

    public StreamException(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
    
}
