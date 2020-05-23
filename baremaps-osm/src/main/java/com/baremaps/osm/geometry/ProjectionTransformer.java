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
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

public class ProjectionTransformer extends GeometryTransformer {

  private final CoordinateTransform coordinateTransform;

  public ProjectionTransformer(CoordinateTransform coordinateTransform) {
    this.coordinateTransform = coordinateTransform;
  }

  @Override
  protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
    Coordinate[] coordinates = Stream.of(coords.toCoordinateArray())
        .map(this::transformCoordinate)
        .toArray(Coordinate[]::new);
    return new CoordinateArraySequence(coordinates);
  }

  private Coordinate transformCoordinate(Coordinate coordinate) {
    ProjCoordinate c1 = new ProjCoordinate(coordinate.x, coordinate.y);
    ProjCoordinate c2 = coordinateTransform.transform(c1, new ProjCoordinate());
    return new Coordinate(c2.x, c2.y);
  }

}
