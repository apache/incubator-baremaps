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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * A data type for {@link GeometryCollection} objects.
 */
public class GeometryCollectionDataType implements DataType<GeometryCollection> {

  private final GeometryFactory geometryFactory;

  private GeometryDataType geometryDataType;

  /**
   * Constructs a {@code GeometryCollectionDataType} with a default {@code GeometryFactory}.
   */
  public GeometryCollectionDataType() {
    this(new GeometryFactory(), new GeometryDataType());
  }

  /**
   * Constructs a {@code GeometryCollectionDataType} with a specified {@code GeometryFactory}.
   *
   * @param geometryFactory the geometry factory
   */
  public GeometryCollectionDataType(GeometryFactory geometryFactory) {
    this(geometryFactory, new GeometryDataType());
  }

  /**
   * Constructs a {@code GeometryCollectionDataType} with a specified {@code GeometryFactory} and
   * {@code GeometryDataType}.
   *
   * @param geometryFactory the geometry factory
   * @param geometryDataType the geometry data type
   */
  public GeometryCollectionDataType(GeometryFactory geometryFactory,
      GeometryDataType geometryDataType) {
    this.geometryFactory = geometryFactory;
    this.geometryDataType = geometryDataType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(final GeometryCollection value) {
    int size = Integer.BYTES;
    for (int i = 0; i < value.getNumGeometries(); i++) {
      size += geometryDataType.size(value.getGeometryN(i));
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
  public void write(final ByteBuffer buffer, final int position, final GeometryCollection value) {
    buffer.putInt(position, size(value));
    var p = position + Integer.BYTES;
    for (int i = 0; i < value.getNumGeometries(); i++) {
      var geometry = value.getGeometryN(i);
      geometryDataType.write(buffer, p, geometry);
      p += geometryDataType.size(buffer, p);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GeometryCollection read(final ByteBuffer buffer, final int position) {
    var size = size(buffer, position);
    var limit = position + size;
    var p = position + Integer.BYTES;
    var geometries = new ArrayList<Geometry>();
    while (p < limit) {
      var geometry = geometryDataType.read(buffer, p);
      geometries.add(geometry);
      p += geometryDataType.size(geometry);
    }
    return geometryFactory.createGeometryCollection(geometries.toArray(Geometry[]::new));
  }
}
