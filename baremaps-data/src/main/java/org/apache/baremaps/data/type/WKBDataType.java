/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.data.type;



import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import java.nio.ByteBuffer;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

/** A {@link DataType} for reading and writing {@link Geometry} in {@link ByteBuffer}s. */
public class WKBDataType implements DataType<Geometry> {

  /** {@inheritDoc} */
  @Override
  public int size(final Geometry value) {
    byte[] bytes = serialize(value);
    if (bytes == null) {
      return Integer.BYTES;
    } else {
      return Integer.BYTES + bytes.length;
    }
  }

  /** {@inheritDoc} */
  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final ByteBuffer buffer, final int position, final Geometry value) {
    byte[] bytes = serialize(value);
    if (bytes == null) {
      buffer.putInt(position, Integer.BYTES);
    } else {
      buffer.putInt(position, Integer.BYTES + bytes.length);
      buffer.put(position + Integer.BYTES, bytes);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Geometry read(final ByteBuffer buffer, final int position) {
    int size = buffer.getInt(position);
    byte[] bytes = new byte[Math.max(size - Integer.BYTES, 0)];
    buffer.get(position + Integer.BYTES, bytes);
    return deserialize(bytes);
  }

  public static final GeometryFactory GEOMETRY_FACTORY_WGS84 =
      new GeometryFactory(new PrecisionModel(), 4326);

  /**
   * Serializes a geometry in the WKB format.
   *
   * @param geometry
   * @return
   */
  private static byte[] serialize(Geometry geometry) {
    if (geometry == null) {
      return null;
    }
    WKBWriter writer = new WKBWriter(2, wkbNDR, true);
    return writer.write(geometry);
  }

  /**
   * Deserializes a geometry in the WKB format.
   *
   * @param wkb
   * @return
   */
  private static Geometry deserialize(byte[] wkb) {
    if (wkb == null) {
      return null;
    }
    try {
      WKBReader reader = new WKBReader(new GeometryFactory());
      return reader.read(wkb);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
