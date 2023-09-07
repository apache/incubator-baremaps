package org.apache.baremaps.tilestore.pmtiles;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PMTilesReader {

    private final Path path;

    private Header header;

    private List<Entry> rootEntries;

    public PMTilesReader(Path path) {
        this.path = path;
    }

    public Header getHeader() {
        if (header == null) {
            try (var channel = Files.newByteChannel(path)) {
                var buffer = ByteBuffer.allocate(127);
                channel.read(buffer);
                buffer.flip();
                header = PMTiles.decodeHeader(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return header;
    }

    public List<Entry> getRootDirectory() {
        if (rootEntries == null) {
            var header = getHeader();
            rootEntries = getDirectory(header.getRootDirectoryOffset(), (int) header.getRootDirectoryLength());
        }
        return rootEntries;
    }

    public List<Entry> getDirectory(long offset, int length) {
        var header = getHeader();
        try (var channel = Files.newByteChannel(path)) {
            var compressed = ByteBuffer.allocate(length);
            channel.position(offset);
            channel.read(compressed);
            compressed.flip();
            var decompressed = header.getInternalCompression().decompress(compressed);
            return PMTiles.decodeDirectory(decompressed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuffer getTile(int z, long x, long y) {
        var tileId = PMTiles.zxyToTileId(z, x, y);
        var header = getHeader();
        var entries = getRootDirectory();
        var entry = PMTiles.findTile(entries, tileId);

        if (entry == null) {
            return null;
        }

        try (var channel = Files.newByteChannel(path)) {
            var compressed = ByteBuffer.allocate((int) entry.getLength());
            channel.position(header.getTileDataOffset() + entry.getOffset());
            channel.read(compressed);
            compressed.flip();
            return header.getTileCompression().decompress(compressed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
