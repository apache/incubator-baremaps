package io.gazetteer.core;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Tile {

    private final byte[] bytes;

    public Tile(byte[] bytes) {
        checkNotNull(bytes);
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return Arrays.equals(bytes, tile.bytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}