/*
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

package org.apache.baremaps.database.tile;



import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.math.IntMath;
import com.google.common.math.LongMath;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.locationtech.jts.geom.Envelope;

/** A {@code TileCoord} represents tile coordinate based on a square extent within a projection. */
public final class TileCoord implements Comparable<TileCoord> {

  private static final double EPSILON = 0.0000001;

  private static final int[] sides = IntStream.range(0, 30).map(i -> IntMath.pow(2, i)).toArray();

  private static final long[] squares =
      LongStream.range(0, 30).map(i -> LongMath.pow(IntMath.pow(2, (int) i), 2)).toArray();

  private static final long[] offsets = LongStream.range(0, 30)
      .map(i -> LongStream.range(0, i).map(j -> LongMath.pow(IntMath.pow(2, (int) j), 2)).sum())
      .toArray();

  private final int x;

  private final int y;

  private final int z;

  /**
   * Constructs a tile coordinate from its index.
   *
   * @param index the index
   */
  public TileCoord(int index) {
    int zoom = 0;
    long offset = 0;
    long count = 1;
    while (index >= offset + count) {
      zoom += 1;
      offset += count;
      count = squares[zoom];
    }
    long position = index - offset;
    x = (int) position % sides[zoom];
    y = (int) position / sides[zoom];
    z = zoom;
  }

  /**
   * Constructs a tile coordinate from its coordinates.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the zoom level
   */
  public TileCoord(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Return an iterator for the tile coordinates that overlap with an envelope.
   *
   * @param envelope the envelope
   * @param minzoom the minimum zoom level
   * @param maxzoom the maximum zoom level
   * @return the iterator
   */
  public static Iterator<TileCoord> iterator(Envelope envelope, int minzoom, int maxzoom) {
    return new TileCoordIterator(envelope, minzoom, maxzoom);
  }

  /**
   * Return a list for the tile coordinates that overlap with an envelope.
   *
   * @param envelope the envelope
   * @param minzoom the minimum zoom level
   * @param maxzoom the maximum zoom level
   * @return the iterator
   */
  public static List<TileCoord> list(Envelope envelope, int minzoom, int maxzoom) {
    return ImmutableList.copyOf(iterator(envelope, minzoom, maxzoom));
  }

  /**
   * Counts the tile coordinates that overlap with an envelope.
   *
   * @param envelope the envelope
   * @param minzoom the minimum zoom level
   * @param maxzoom the maximum zoom level
   * @return the count
   */
  public static long count(Envelope envelope, int minzoom, int maxzoom) {
    int count = 0;
    for (int zoom = minzoom; zoom <= maxzoom; zoom++) {
      TileCoord min = min(envelope, zoom);
      TileCoord max = max(envelope, zoom);
      count += (max.x() - min.x() + 1) * (max.y() - min.y() + 1);
    }
    return count;
  }

  /**
   * Returns the tile coordinate at a given longitude, latitude, and zoom.
   *
   * @param lon the longitude
   * @param lat the latitude
   * @param z the zoom level
   * @return the tile
   */
  public static TileCoord fromLonLat(double lon, double lat, int z) {
    int x = (int) ((lon + 180.0) / 360.0 * (1 << z));
    int y = (int) ((1
        - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI)
        / 2.0 * (1 << z));
    return new TileCoord(x, y, z);
  }

  /**
   * Returns the index of the tile coordinate.
   *
   * @return the index
   */
  public long index() {
    long x = this.x;
    long y = this.y;
    long offset = offsets[z];
    long position = x + y * sides[z];
    return offset + position;
  }

  /**
   * Returns the x coordinate of the tile coordinate.
   *
   * @return the x coordinate
   */
  public int x() {
    return x;
  }

  /**
   * Returns the y coordinate of the tile coordinate.
   *
   * @return the y coordinate
   */
  public int y() {
    return y;
  }

  /**
   * Returns the zoom level of the tile coordinate.
   *
   * @return the zoom level
   */
  public int z() {
    return z;
  }

  /**
   * Returns the parent tile coordinate in the pyramid.
   *
   * @return the parent tile
   */
  public TileCoord parent() {
    return new TileCoord(x / 2, y / 2, z - 1);
  }

  /**
   * Returns the envelope of the tile coordinate.
   *
   * @return the envelope
   */
  public Envelope envelope() {
    double x1 = tile2lon(x, z);
    double x2 = tile2lon(x + 1, z);
    double y1 = tile2lat(y + 1, z);
    double y2 = tile2lat(y, z);
    return new Envelope(x1, x2, y1, y2);
  }

  public static double tile2lon(int x, int z) {
    return x / Math.pow(2.0, z) * 360.0 - 180;
  }

  public static double tile2lat(int y, int z) {
    double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }

  public static TileCoord min(Envelope envelope, int zoom) {
    return TileCoord.fromLonLat(envelope.getMinX(), envelope.getMaxY(), zoom);
  }

  public static TileCoord max(Envelope envelope, int zoom) {
    return TileCoord.fromLonLat(envelope.getMaxX() - EPSILON, envelope.getMinY() + EPSILON, zoom);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TileCoord that = (TileCoord) o;
    return x == that.x && y == that.y && z == that.z;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(x, y, z);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("x", x).add("y", y).add("z", z).toString();
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(TileCoord that) {
    return Long.compare(this.index(), that.index());
  }
}
