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

package com.baremaps.osm.geometry;

import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

/** A transformer that reprojects geometries and sets the correct output SRIDs. */
public class ProjectionTransformer extends GeometryTransformer {

  private final int inputSRID;

  private final int outputSRID;

  private final CoordinateTransform coordinateTransform;

  /**
   * Creates a transformer that reprojects geometries with the provided SRIDs.
   *
   * @param inputSRID the input SRID
   * @param outputSRID the output SRID
   */
  public ProjectionTransformer(int inputSRID, int outputSRID) {
    this.inputSRID = inputSRID;
    this.outputSRID = outputSRID;
    this.coordinateTransform = GeometryUtils.coordinateTransform(inputSRID, outputSRID);
  }

  @Override
  protected CoordinateSequence transformCoordinates(
      CoordinateSequence coordinateSequence, Geometry parent) {
    Coordinate[] coordinateArray =
        Stream.of(coordinateSequence.toCoordinateArray())
            .map(this::transformCoordinate)
            .toArray(Coordinate[]::new);
    return new CoordinateArraySequence(coordinateArray);
  }

  private Coordinate transformCoordinate(Coordinate coordinate) {
    ProjCoordinate c1 = new ProjCoordinate(coordinate.x, coordinate.y);
    ProjCoordinate c2 = coordinateTransform.transform(c1, new ProjCoordinate());
    return new Coordinate(c2.x, c2.y);
  }

  protected Geometry transformPoint(Point geom, Geometry parent) {
    return withTargetSRID(super.transformPoint(geom, parent));
  }

  protected Geometry transformMultiPoint(MultiPoint geom, Geometry parent) {
    return withTargetSRID(super.transformMultiPoint(geom, parent));
  }

  protected Geometry transformLinearRing(LinearRing geom, Geometry parent) {
    return withTargetSRID(super.transformLinearRing(geom, parent));
  }

  protected Geometry transformLineString(LineString geom, Geometry parent) {
    return withTargetSRID(super.transformLineString(geom, parent));
  }

  protected Geometry transformMultiLineString(MultiLineString geom, Geometry parent) {
    return withTargetSRID(super.transformMultiLineString(geom, parent));
  }

  protected Geometry transformPolygon(Polygon geom, Geometry parent) {
    return withTargetSRID(super.transformPolygon(geom, parent));
  }

  protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
    return withTargetSRID(super.transformMultiPolygon(geom, parent));
  }

  protected Geometry transformGeometryCollection(GeometryCollection geom, Geometry parent) {
    return withTargetSRID(super.transformGeometryCollection(geom, parent));
  }

  private Geometry withTargetSRID(Geometry outputGeom) {
    outputGeom.setSRID(outputSRID);
    return outputGeom;
  }
}
