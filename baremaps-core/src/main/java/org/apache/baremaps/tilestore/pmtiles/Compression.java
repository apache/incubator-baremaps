package org.apache.baremaps.tilestore.pmtiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

enum Compression {
    Unknown,
    None,
    Gzip,
    Brotli,
    Zstd;

    ByteBuffer decompress(ByteBuffer buffer) {
        return switch (this) {
            case None -> buffer;
            case Gzip -> decompressGzip(buffer);
            case Brotli -> decompressBrotli(buffer);
            case Zstd -> decompressZstd(buffer);
            default -> throw new RuntimeException("Unknown compression");
        };
    }

    static ByteBuffer decompressGzip(ByteBuffer buffer) {
        try(var inputStream = new GZIPInputStream(new ByteArrayInputStream(buffer.array()))) {
            return ByteBuffer.wrap(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ByteBuffer decompressBrotli(ByteBuffer buffer) {
        throw new RuntimeException("Brotli compression not implemented");
    }

    static ByteBuffer decompressZstd(ByteBuffer buffer) {
        throw new RuntimeException("Zstd compression not implemented");
    }

    ByteBuffer compress(ByteBuffer buffer) {
        return switch (this) {
            case None -> buffer;
            case Gzip -> compressGzip(buffer);
            case Brotli -> compressBrotli(buffer);
            case Zstd -> compressZstd(buffer);
            default -> throw new RuntimeException("Unknown compression");
        };
    }

    static ByteBuffer compressGzip(ByteBuffer buffer) {
        try(var outputStream = new ByteArrayOutputStream();
        var gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(buffer.array());
            return ByteBuffer.wrap(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ByteBuffer compressBrotli(ByteBuffer buffer) {
        throw new RuntimeException("Brotli compression not implemented");
    }

    static ByteBuffer compressZstd(ByteBuffer buffer) {
        throw new RuntimeException("Zstd compression not implemented");
    }
}
