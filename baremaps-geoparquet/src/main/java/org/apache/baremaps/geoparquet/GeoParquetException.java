package org.apache.baremaps.geoparquet;

public class GeoParquetException extends RuntimeException {
    public GeoParquetException(String message) {
        super(message);
    }

    public GeoParquetException(String message, Throwable cause) {
        super(message, cause);
    }
}
