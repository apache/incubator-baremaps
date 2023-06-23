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

package org.apache.baremaps.database.type.geometry;

import java.nio.ByteBuffer;
import org.apache.baremaps.database.type.DataType;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * A data type for {@link LineString} objects.
 */
public class LineStringDataType implements DataType<LineString> {

  private final GeometryFactory geometryFactory;

  private final CoordinateArrayDataType coordinateArrayDataType;

  /**
   * Constructs a {@code LineStringDataType} with a default {@code GeometryFactory}.
   */
  public LineStringDataType() {
    this(new GeometryFactory());
  }

  /**
   * Constructs a {@code LineStringDataType} with a specified {@code GeometryFactory}.
   *
   * @param geometryFactory the geometry factory
   */
  public LineStringDataType(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.coordinateArrayDataType = new CoordinateArrayDataType();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(final LineString value) {
    return coordinateArrayDataType.size(value.getCoordinates());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return coordinateArrayDataType.size(buffer, position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(final ByteBuffer buffer, final int position, final LineString value) {
    coordinateArrayDataType.write(buffer, position, value.getCoordinates());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LineString read(final ByteBuffer buffer, final int position) {
    var coordinates = coordinateArrayDataType.read(buffer, position);
    return geometryFactory.createLineString(coordinates);
  }
}
