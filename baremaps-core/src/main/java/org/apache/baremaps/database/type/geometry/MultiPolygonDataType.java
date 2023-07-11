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
import java.util.ArrayList;
import org.apache.baremaps.database.type.DataType;
import org.locationtech.jts.geom.*;

/**
 * A data type for {@link GeometryCollection} objects.
 */
public class MultiPolygonDataType implements DataType<MultiPolygon> {

  private final GeometryFactory geometryFactory;

  private final PolygonDataType polygonDataType;

  /**
   * Constructs a {@code MultiPolygonDataType} with a default {@code GeometryFactory}.
   */
  public MultiPolygonDataType() {
    this(new GeometryFactory());
  }

  /**
   * Constructs a {@code MultiPolygonDataType} with a specified {@code GeometryFactory}.
   *
   * @param geometryFactory the geometry factory
   */
  public MultiPolygonDataType(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.polygonDataType = new PolygonDataType(geometryFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(final MultiPolygon value) {
    int size = Integer.BYTES;
    for (int i = 0; i < value.getNumGeometries(); i++) {
      size += polygonDataType.size((Polygon) value.getGeometryN(i));
    }
    return size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(final ByteBuffer buffer, final int position, final MultiPolygon value) {
    buffer.putInt(position, size(value));
    var p = position + Integer.BYTES;
    for (int i = 0; i < value.getNumGeometries(); i++) {
      polygonDataType.write(buffer, p, (Polygon) value.getGeometryN(i));
      p += buffer.getInt(p);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MultiPolygon read(final ByteBuffer buffer, final int position) {
    var size = size(buffer, position);
    var limit = position + size;
    var p = position + Integer.BYTES;
    var polygons = new ArrayList<Polygon>();
    while (p < limit) {
      var polygon = polygonDataType.read(buffer, p);
      polygons.add(polygon);
      p += polygonDataType.size(buffer, p);
    }
    return geometryFactory.createMultiPolygon(polygons.toArray(Polygon[]::new));
  }
}
