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

package com.baremaps.osm.cache;

import java.nio.ByteBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;

public class LmdbCoordinateCache extends LmdbCache<Long, Coordinate> {

  public LmdbCoordinateCache(Env<ByteBuffer> env, Dbi<ByteBuffer> database) {
    super(env, database);
  }

  @Override
  public ByteBuffer buffer(Long key) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(20);
    buffer.put(String.format("%020d", key).getBytes()).flip();
    return buffer.putLong(key);
  }

  @Override
  public Coordinate read(ByteBuffer buffer) {
    if (buffer == null || buffer.get() == 0) {
      return null;
    }
    double lon = buffer.getDouble();
    double lat = buffer.getDouble();
    return new Coordinate(lon, lat);
  }

  @Override
  public ByteBuffer write(Coordinate value) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(17);
    if (value != null) {
      buffer.put((byte) 1);
      buffer.putDouble(value.getX());
      buffer.putDouble(value.getY());
    }
    buffer.flip();
    return buffer;
  }

}
