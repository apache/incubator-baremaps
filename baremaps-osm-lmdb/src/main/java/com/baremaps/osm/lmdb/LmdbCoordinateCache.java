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

package com.baremaps.osm.lmdb;

import com.baremaps.osm.cache.CoordinateCache;
import java.nio.ByteBuffer;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;

public class LmdbCoordinateCache extends LmdbCache<Long, Coordinate> implements CoordinateCache {

  public LmdbCoordinateCache(Env<ByteBuffer> env) {
    super(env, env.openDbi("coordinate", DbiFlags.MDB_CREATE));
  }

  @Override
  public ByteBuffer buffer(Long key) {
    return ByteBuffer.allocateDirect(Long.BYTES).putLong(key).flip();
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
