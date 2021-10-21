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

package com.baremaps.osm.rocksdb;

import java.nio.ByteBuffer;
import org.locationtech.jts.geom.Coordinate;
import org.rocksdb.RocksDB;

/** A {@code Cache} for coordinates baked by RocksDB. */
public class RocksdbCoordinateCache extends RocksdbCache<Long, Coordinate> {

  /** Constructs a {@code RocksdbCoordinateCache}. */
  public RocksdbCoordinateCache(RocksDB db) {
    super(db);
  }

  @Override
  protected byte[] key(Long key) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(key);
    return buffer.array();
  }

  @Override
  protected Coordinate read(byte[] array) {
    if (array == null) {
      return null;
    }
    ByteBuffer buffer = ByteBuffer.wrap(array);
    if (buffer.get() == 0) {
      return null;
    }
    double lon = buffer.getDouble();
    double lat = buffer.getDouble();
    return new Coordinate(lon, lat);
  }

  @Override
  protected byte[] write(Coordinate value) {
    ByteBuffer buffer = ByteBuffer.allocate(17);
    if (value != null) {
      buffer.put((byte) 1);
      buffer.putDouble(value.getX());
      buffer.putDouble(value.getY());
    }
    buffer.flip();
    return buffer.array();
  }
}
