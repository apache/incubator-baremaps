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

package org.apache.baremaps.collection.type.geometry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

/**
 * A data type for {@link MultiLineString} objects.
 */
public class MultiLineStringDataType implements DataType<MultiLineString> {

  private final LineStringDataType lineStringDataType;

  private final GeometryFactory geometryFactory;

  /**
   * Constructs a {@code MultiLineStringDataType} with a default {@code GeometryFactory}.
   */
  public MultiLineStringDataType() {
    this(new GeometryFactory());
  }

  /**
   * Constructs a {@code MultiLineStringDataType} with a specified {@code GeometryFactory}.
   *
   * @param geometryFactory the geometry factory
   */
  public MultiLineStringDataType(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.lineStringDataType = new LineStringDataType(geometryFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(MultiLineString value) {
    int size = Integer.BYTES;
    for (int i = 0; i < value.getNumGeometries(); i++) {
      size += lineStringDataType.size((LineString) value.getGeometryN(i));
    }
    return size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(ByteBuffer buffer, int position, MultiLineString value) {
    buffer.putInt(position, size(value));
    position += Integer.BYTES;
    for (int i = 0; i < value.getNumGeometries(); i++) {
      lineStringDataType.write(buffer, position, (LineString) value.getGeometryN(i));
      position += buffer.getInt(position);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MultiLineString read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += Integer.BYTES;
    var lineStrings = new ArrayList<LineString>();
    while (position < size) {
      var lineString = lineStringDataType.read(buffer, position);
      lineStrings.add(lineString);
      position += lineStringDataType.size(buffer, position);
    }
    return geometryFactory.createMultiLineString(lineStrings.toArray(LineString[]::new));
  }
}
