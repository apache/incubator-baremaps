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
import org.locationtech.jts.geom.Coordinate;

public class CoordinateMapper implements CacheMapper<Coordinate> {

  @Override
  public int size(Coordinate value) {
    return 17;
  }

  @Override
  public Coordinate read(ByteBuffer buffer) {
    if (buffer == null) {
      return null;
    }
    if (buffer.get() == 0) {
      buffer.flip();
      return null;
    }
    double lon = buffer.getDouble();
    double lat = buffer.getDouble();
    buffer.flip();
    return new Coordinate(lon, lat);
  }

  @Override
  public void write(ByteBuffer buffer, Coordinate value) {
    if (value != null) {
      buffer.put((byte) 1);
      buffer.putDouble(value.getX());
      buffer.putDouble(value.getY());
    }
    buffer.flip();
  }
}
