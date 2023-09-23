package org.apache.baremaps.utils;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public enum Compression {
    none,
    gzip,
    bzip2;

    public static Compression detect(Path file) {
        if (file.toString().endsWith(".gz")) {
            return gzip;
        } else if (file.toString().endsWith(".bz2")) {
            return bzip2;
        } else {
            return none;
        }
    }

    public InputStream decompress(InputStream inputStream) throws IOException {
        return switch (this) {
            case gzip -> new GZIPInputStream(inputStream);
            case bzip2 -> new BZip2CompressorInputStream(inputStream);
            default -> inputStream;
        };
    }

    public OutputStream compress(OutputStream outputStream) throws IOException {
        return switch (this) {
            case gzip -> new GZIPOutputStream(outputStream);
            case bzip2 -> new BZip2CompressorOutputStream(outputStream);
            default -> outputStream;
        };
    }
}
