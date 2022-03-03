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

package com.baremaps.store.type;

import java.nio.ByteBuffer;
import org.locationtech.jts.geom.Coordinate;

/**
 * A {@link DataType} for reading and writing longitude/latitude coordinates in {@link ByteBuffer}s.
 * An integer is used to compress the coordinates to the detriment of precision (centimeters).
 */
public class LonLatDataType implements AlignedDataType<Coordinate> {

  private static final long LOWER_32_BIT_MASK = (1L << 32) - 1L;

  public static double decodeLat(long encoded) {
    return (double) (encoded & LOWER_32_BIT_MASK) / 10000000;
  }

  public static double decodeLon(long encoded) {
    return (double) (encoded >>> 32) / 10000000;
  }

  public static long encodeLonLat(double lon, double lat) {
    long x = (long) (lon * 10000000);
    long y = (long) (lat * 10000000);
    return (x << 32) | (y & LOWER_32_BIT_MASK);
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
