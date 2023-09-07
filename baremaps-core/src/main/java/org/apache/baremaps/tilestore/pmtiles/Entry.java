package org.apache.baremaps.tilestore.pmtiles;

public class Entry {
    private long tileId;
    private long offset;
    private long length;
    private long runLength;

    public Entry() {

    }

    public Entry(long tileId, long offset, long length, long runLength) {
        this.tileId = tileId;
        this.offset = offset;
        this.length = length;
        this.runLength = runLength;
    }

    public long getTileId() {
        return tileId;
    }

    public void setTileId(long tileId) {
        this.tileId = tileId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getRunLength() {
        return runLength;
    }

    public void setRunLength(long runLength) {
        this.runLength = runLength;
    }
}
