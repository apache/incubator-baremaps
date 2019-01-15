package io.gazetteer.mbtiles;

import mil.nga.sf.GeometryEnvelope;

public class Coordinate {

    public final int z, x, y;

    public Coordinate(int z, int x, int y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        if (z != that.z) return false;
        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = z;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }

    public GeometryEnvelope envelope() {
        double north = tile2lat(y, z);
        double south = tile2lat(y + 1, z);
        double west = tile2lon(x, z);
        double east = tile2lon(x + 1, z);
        return new GeometryEnvelope(west, south, east, north);
    }

    public static double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}