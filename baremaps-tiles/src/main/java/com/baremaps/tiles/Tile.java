/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.tiles;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.math.IntMath;
import com.google.common.math.LongMath;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Envelope;

public final class Tile {

  private static final int[] sides = IntStream.range(0, 30)
      .map(i -> IntMath.pow(2, i))
      .toArray();

  private static final long[] squares = LongStream.range(0, 30)
      .map(i -> LongMath.pow(IntMath.pow(2, (int) i), 2))
      .toArray();

  private static final long[] offsets = LongStream.range(0, 30).map(i -> LongStream.range(0, i)
      .map(j -> LongMath.pow(IntMath.pow(2, (int) j), 2)).sum())
      .toArray();

  private final int x, y, z;

  public Tile(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public long index() {
    long x = this.x;
    long y = this.y;
    long offset = offsets[z];
    long position = x + y * sides[z];
    return offset + position;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }

  public int z() {
    return z;
  }

  public Tile parent() {
    return new Tile(x / 2, y / 2, z - 1);
  }

  public List<Tile> children() {
    return Arrays.asList(
        new Tile(2 * x, 2 * y, z + 1),
        new Tile(2 * x + 1, 2 * y, z + 1),
        new Tile(2 * x, 2 * y + 1, z + 1),
        new Tile(2 * x + 1, 2 * y + 1, z + 1));
  }

  public Envelope envelope() {
    double north = tile2lat(y, z);
    double south = tile2lat(y + 1, z);
    double west = tile2lon(x, z);
    double east = tile2lon(x + 1, z);
    return new Envelope(west, east, south, north);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Tile that = (Tile) o;
    return x == that.x && y == that.y && z == that.z;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(x, y, z);
  }

  public static double tile2lon(int x, int z) {
    return x / Math.pow(2.0, z) * 360.0 - 180;
  }

  public static double tile2lat(int y, int z) {
    double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("x", x)
        .add("y", y)
        .add("z", z)
        .toString();
  }

  public static long countTiles(Envelope envelope, int minZ, int maxZ) {
    return IntStream.rangeClosed(minZ, maxZ).mapToLong(z -> countTiles(envelope, z)).sum();
  }

  public static long countTiles(Envelope envelope, int z) {
    Tile min = fromLonLat(envelope.getMinX(), envelope.getMaxY(), z);
    Tile max = fromLonLat(envelope.getMaxX(), envelope.getMinY(), z);
    return (max.x() - min.x() + 1) * (max.y() - min.y() + 1);
  }

  public static Stream<Tile> getTiles(Envelope envelope, int minZ, int maxZ) {
    return IntStream.rangeClosed(minZ, maxZ).boxed().flatMap(z -> getTiles(envelope, z));
  }

  public static Stream<Tile> getTiles(Envelope envelope, int z) {
    if (envelope == null) {
      return Stream.empty();
    }
    Tile min = fromLonLat(envelope.getMinX(), envelope.getMaxY(), z);
    Tile max = fromLonLat(envelope.getMaxX(), envelope.getMinY(), z);
    return IntStream.rangeClosed(min.x(), max.x()).boxed()
        .flatMap(x -> IntStream
            .rangeClosed(min.y(), max.y())
            .boxed()
            .map(y -> new Tile(x, y, z)));
  }

  public static Tile fromLonLat(double lon, double lat, int z) {
    int x = (int) ((lon + 180.0) / 360.0 * (1 << z));
    int y = (int) ((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat)))
        / Math.PI) / 2.0 * (1 << z));
    return new Tile(x, y, z);
  }

  public static Tile fromIndex(Long value) {
    int zoom = 0;
    long offset = 0;
    long count = 1;
    while (value >= offset + count) {
      zoom += 1;
      offset += count;
      count = squares[zoom];
    }
    long position = value - offset;
    int x = (int) position % sides[zoom];
    int y = (int) position / sides[zoom];
    return new Tile(x, y, zoom);
  }

}
