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
import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.*;

/**
 * A {@code DataType} for {@link Geometry} objects.
 */
public class GeometryDataType implements DataType<Geometry> {

  private final PointDataType pointDataType;

  private final LineStringDataType lineStringDataType;

  private final PolygonDataType polygonDataType;

  private final MultiPointDataType multiPointDataType;

  private final MultiLineStringDataType multiLineStringDataType;

  private final MultiPolygonDataType multiPolygonDataType;

  private final GeometryCollectionDataType geometryCollectionDataType;

  /**
   * Constructs a {@code GeometryDataType} with a default {@code GeometryFactory}.
   */
  public GeometryDataType() {
    this(new GeometryFactory());
  }

  /**
   * Constructs a {@code GeometryDataType} with a specified {@code GeometryFactory}.
   *
   * @param geometryFactory
   */
  public GeometryDataType(GeometryFactory geometryFactory) {
    this.pointDataType = new PointDataType(geometryFactory);
    this.lineStringDataType = new LineStringDataType(geometryFactory);
    this.polygonDataType = new PolygonDataType(geometryFactory);
    this.multiPointDataType = new MultiPointDataType(geometryFactory);
    this.multiLineStringDataType = new MultiLineStringDataType(geometryFactory);
    this.multiPolygonDataType = new MultiPolygonDataType(geometryFactory);
    this.geometryCollectionDataType = new GeometryCollectionDataType(geometryFactory, this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(Geometry value) {
    var size = 0;

    // Geometry type
    size += Byte.BYTES;

    // Size of the geometry
    if (value instanceof Point point) {
      size += pointDataType.size(point);
    } else if (value instanceof LineString lineString) {
      size += lineStringDataType.size(lineString);
    } else if (value instanceof Polygon polygon) {
      size += polygonDataType.size(polygon);
    } else if (value instanceof MultiPoint multiPoint) {
      size += multiPointDataType.size(multiPoint);
    } else if (value instanceof MultiLineString multiLineString) {
      size += multiLineStringDataType.size(multiLineString);
    } else if (value instanceof MultiPolygon multiPolygon) {
      size += multiPolygonDataType.size(multiPolygon);
    } else if (value instanceof GeometryCollection geometryCollection) {
      size += geometryCollectionDataType.size(geometryCollection);
    } else {
      throw new IllegalArgumentException("Unsupported geometry type: " + value.getClass());
    }

    return size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(ByteBuffer buffer, int position) {
    var size = 0;

    // Geometry type
    var type = buffer.get(position);
    size += Byte.BYTES;

    // Size of the geometry
    if (type == 1) {
      size += pointDataType.size(buffer, position + Byte.BYTES);
    } else if (type == 2) {
      size += lineStringDataType.size(buffer, position + Byte.BYTES);
    } else if (type == 3) {
      size += polygonDataType.size(buffer, position + Byte.BYTES);
    } else if (type == 4) {
      size += multiPointDataType.size(buffer, position + Byte.BYTES);
    } else if (type == 5) {
      size += multiLineStringDataType.size(buffer, position + Byte.BYTES);
    } else if (type == 6) {
      size += multiPolygonDataType.size(buffer, position + Byte.BYTES);
    } else if (type == 7) {
      size += geometryCollectionDataType.size(buffer, position + Byte.BYTES);
    } else {
      throw new IllegalArgumentException("Unsupported geometry type: " + type);
    }

    return size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(ByteBuffer buffer, int position, Geometry value) {
    // Write the geometry
    if (value == null) {
      buffer.put(position, (byte) 0);
    } else if (value instanceof Point point) {
      buffer.put(position, (byte) 1);
      position += Byte.BYTES;
      pointDataType.write(buffer, position, point);
    } else if (value instanceof LineString lineString) {
      buffer.put(position, (byte) 2);
      position += Byte.BYTES;
      lineStringDataType.write(buffer, position, lineString);
    } else if (value instanceof Polygon polygon) {
      buffer.put(position, (byte) 3);
      position += Byte.BYTES;
      polygonDataType.write(buffer, position, polygon);
    } else if (value instanceof MultiPoint multiPoint) {
      buffer.put(position, (byte) 4);
      position += Byte.BYTES;
      multiPointDataType.write(buffer, position, multiPoint);
    } else if (value instanceof MultiLineString multiLineString) {
      buffer.put(position, (byte) 5);
      position += Byte.BYTES;
      multiLineStringDataType.write(buffer, position, multiLineString);
    } else if (value instanceof MultiPolygon multiPolygon) {
      buffer.put(position, (byte) 6);
      position += Byte.BYTES;
      multiPolygonDataType.write(buffer, position, multiPolygon);
    } else if (value instanceof GeometryCollection geometryCollection) {
      buffer.put(position, (byte) 7);
      position += Byte.BYTES;
      geometryCollectionDataType.write(buffer, position, geometryCollection);
    } else {
      throw new IllegalArgumentException("Unsupported geometry type: " + value.getClass());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Geometry read(ByteBuffer buffer, int position) {
    // Read the geometry type
    var type = buffer.get(position);
    position += Byte.BYTES;

    // Read the geometry
    if (type == 0) {
      return null;
    } else if (type == 1) {
      return pointDataType.read(buffer, position);
    } else if (type == 2) {
      return lineStringDataType.read(buffer, position);
    } else if (type == 3) {
      return polygonDataType.read(buffer, position);
    } else if (type == 4) {
      return multiPointDataType.read(buffer, position);
    } else if (type == 5) {
      return multiLineStringDataType.read(buffer, position);
    } else if (type == 6) {
      return multiPolygonDataType.read(buffer, position);
    } else if (type == 7) {
      return geometryCollectionDataType.read(buffer, position);
    } else {
      throw new IllegalArgumentException("Unsupported geometry type: " + type);
    }
  }
}
