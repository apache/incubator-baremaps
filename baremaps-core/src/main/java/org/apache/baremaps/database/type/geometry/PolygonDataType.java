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

package org.apache.baremaps.database.type.geometry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.apache.baremaps.database.type.DataType;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * A data type for {@link Polygon} objects.
 */
public class PolygonDataType implements DataType<Polygon> {

  private final CoordinateArrayDataType coordinateArrayDataType = new CoordinateArrayDataType();

  private final GeometryFactory geometryFactory;

  /**
   * Constructs a {@code PolygonDataType} with a default {@code GeometryFactory}.
   */
  public PolygonDataType() {
    this(new GeometryFactory());
  }

  /**
   * Constructs a {@code PolygonDataType} with a specified {@code GeometryFactory}.
   *
   * @param geometryFactory the geometry factory
   */
  public PolygonDataType(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public int size(final Polygon value) {
    int size = Integer.BYTES;

    // Add the size of the exterior ring
    var exteriorRing = value.getExteriorRing();
    size += coordinateArrayDataType.size(exteriorRing.getCoordinates());

    // Add the size of the interior rings
    for (int i = 0; i < value.getNumInteriorRing(); i++) {
      var interiorRing = value.getInteriorRingN(i);
      size += coordinateArrayDataType.size(interiorRing.getCoordinates());
    }

    return size;
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
  public void write(final ByteBuffer buffer, final int position, final Polygon value) {
    buffer.putInt(position, size(value));
    var p = position + Integer.BYTES;

    // Write the exterior ring
    var exteriorRing = value.getExteriorRing();
    coordinateArrayDataType.write(buffer, p, exteriorRing.getCoordinates());
    p += coordinateArrayDataType.size(exteriorRing.getCoordinates());

    // Write the interior rings
    for (int i = 0; i < value.getNumInteriorRing(); i++) {
      var interiorRing = value.getInteriorRingN(i);
      coordinateArrayDataType.write(buffer, p, interiorRing.getCoordinates());
      p += coordinateArrayDataType.size(interiorRing.getCoordinates());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Polygon read(final ByteBuffer buffer, final int position) {
    var size = size(buffer, position);
    var limit = position + size;
    var p = position + Integer.BYTES;


    // Read the exterior ring
    var exteriorRingCoordinates = coordinateArrayDataType.read(buffer, p);
    var exteriorRing = geometryFactory.createLinearRing(exteriorRingCoordinates);
    p += coordinateArrayDataType.size(buffer, p);

    // Read the interior rings
    var interiorRings = new ArrayList<LineString>();
    while (p < limit) {
      var interiorRingCoordinates = coordinateArrayDataType.read(buffer, p);
      var interiorRing = geometryFactory.createLinearRing(interiorRingCoordinates);
      interiorRings.add(interiorRing);
      p += coordinateArrayDataType.size(buffer, p);
    }

    return geometryFactory.createPolygon(exteriorRing, interiorRings.toArray(LinearRing[]::new));
  }
}
