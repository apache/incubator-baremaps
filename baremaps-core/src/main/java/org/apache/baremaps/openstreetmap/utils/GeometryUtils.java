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

package org.apache.baremaps.openstreetmap.utils;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
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
      new GeometryFactory(new PrecisionModel(), 4326);

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
}
