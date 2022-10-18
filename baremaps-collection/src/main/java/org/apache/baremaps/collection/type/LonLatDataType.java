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

package org.apache.baremaps.collection.type;



import java.nio.ByteBuffer;
import org.locationtech.jts.geom.Coordinate;

/**
 * A {@link DataType} for reading and writing longitude/latitude coordinates in {@link ByteBuffer}s.
 * An integer is used to compress the coordinates to the detriment of precision (centimeters).
 */
public class LonLatDataType implements SizedDataType<Coordinate> {

  private static final double BITS = Math.pow(2, 31);
  private static final long SHIFT = 32;
  private static final long MASK = (1L << 32) - 1L;

  public static long encodeLonLat(double lon, double lat) {
    long x = (long) (((lon + 180) / 360) * BITS);
    long y = (long) (((lat + 90) / 180) * BITS);
    long l = (x << SHIFT);
    long r = (y & MASK);
    return l | r;
  }

  public static double decodeLon(long value) {
    double l = (value >>> 32);
    return (l / BITS) * 360 - 180;
  }

  public static double decodeLat(long value) {
    long r = (value & MASK);
    return (r / BITS) * 180 - 90;
  }

  /** {@inheritDoc} */
  @Override
  public int size(Coordinate value) {
    return 8;
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, Coordinate value) {
    buffer.putLong(position, encodeLonLat(value.x, value.y));
  }

  /** {@inheritDoc} */
  @Override
  public Coordinate read(ByteBuffer buffer, int position) {
    long value = buffer.getLong(position);
    return new Coordinate(decodeLon(value), decodeLat(value));
  }
}
