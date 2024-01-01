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

package org.apache.baremaps.utils;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;

/** Utility methods for serializing and deserializing geometries. */
public class GeometryUtils {

  public static final GeometryFactory GEOMETRY_FACTORY_WGS84 =
      new GeometryFactory(new PrecisionModel(1000000000), 4326);

  private GeometryUtils() {}

  /**
   * Serializes a geometry in the WKB format.
   *
   * @param geometry
   * @return
   */
  public static byte[] serialize(Geometry geometry) {
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
  public static Geometry deserialize(byte[] wkb) {
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

  /**
   * Creates a coordinate transform with the provided SRIDs.
   *
   * @param sourceSRID the source SRID
   * @param targetSRID the target SRID
   * @return the coordinate transform
   */
  public static CoordinateTransform coordinateTransform(Integer sourceSRID, Integer targetSRID) {
    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem sourceCRS =
        crsFactory.createFromName(String.format("EPSG:%d", sourceSRID));
    CoordinateReferenceSystem targetCRS =
        crsFactory.createFromName(String.format("EPSG:%d", targetSRID));
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    return coordinateTransformFactory.createTransform(sourceCRS, targetCRS);
  }

  /**
   * Creates a projection transformer with the provided SRIDs.
   *
   * @param inputSRID the input SRID
   * @param outputSRID the output SRID
   * @return the projection transformer
   */
  public static ProjectionTransformer projectionTransformer(int inputSRID, int outputSRID) {
    return new ProjectionTransformer(inputSRID, outputSRID);
  }

  public static CoordinateSequence removeExtraPoint(CoordinateSequence coordinates) {
    List<Coordinate> simplifiedList = new ArrayList<>();

    for (int i = 0; i < coordinates.size() - 1; i++) {
      // Add the coordinate if it's not a point that needs to be removed
      if (!isExtraPoint(coordinates, i)) {
        simplifiedList.add(coordinates.getCoordinate(i));
      }
    }

    // Ensure the polygon is closed by adding the first coordinate at the end
    simplifiedList.add(simplifiedList.get(0));

    return new CoordinateArraySequence(simplifiedList.toArray(new Coordinate[0]));
  }

  private static boolean isExtraPoint(CoordinateSequence coordinates, int index) {
    return false;

    // if (index == 0 || index == coordinates.size() - 1) {
    // return false;
    // }
    //
    // Coordinate prev = coordinates.getCoordinate(index - 1);
    // Coordinate current = coordinates.getCoordinate(index);
    // Coordinate next = coordinates.getCoordinate(index + 1);
    //
    // if (prev.x == current.x && current.x == next.x) {
    // return true;
    // }
    //
    // if (prev.y == current.y && current.y == next.y) {
    // return true;
    // }
    //
    // // Calculate slopes between prev-current and current-next
    // double slope1 = (current.y - prev.y) / (current.x - prev.x);
    // double slope2 = (next.y - current.y) / (next.x - current.x);
    //
    // // If slopes are equal, current is an extra point
    // return Double.compare(slope1, slope2) == 0;
  }

}
