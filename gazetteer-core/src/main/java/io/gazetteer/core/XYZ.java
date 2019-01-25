package io.gazetteer.core;

import com.google.common.base.Objects;
import mil.nga.sf.GeometryEnvelope;

public final class XYZ {

  private final int x, y, z;

  public XYZ(int x, int y, int z) {

    this.x = x;
    this.y = y;
    this.z = z;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  public GeometryEnvelope envelope() {
    double north = tile2lat(y, z);
    double south = tile2lat(y + 1, z);
    double west = tile2lon(x, z);
    double east = tile2lon(x + 1, z);
    return new GeometryEnvelope(west, south, east, north);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    XYZ that = (XYZ) o;
    return x == that.x && y == that.y && z == that.z;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(x, y, z);
  }

  private static double tile2lon(int x, int z) {
    return x / Math.pow(2.0, z) * 360.0 - 180;
  }

  private static double tile2lat(int y, int z) {
    double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }
}
