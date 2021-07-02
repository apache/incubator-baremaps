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

import com.baremaps.osm.domain.Element;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ElementConsumer;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

/**
 * Changes the projection of the geometry of an element via side-effects.
 */
public class ProjectionConsumer extends GeometryTransformer implements ElementConsumer {

  private final int dstSRID;

  private final CoordinateTransform coordinateTransform;

  public ProjectionConsumer(int sourceSRID, int targetSRID) {
    this.dstSRID = targetSRID;
    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem sourceCRS = crsFactory.createFromName(String.format("EPSG:%d", sourceSRID));
    CoordinateReferenceSystem targetCRS = crsFactory.createFromName(String.format("EPSG:%d", targetSRID));
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    coordinateTransform = coordinateTransformFactory.createTransform(sourceCRS, targetCRS);
  }

  @Override
  protected CoordinateSequence transformCoordinates(CoordinateSequence coordinateSequence, Geometry parent) {
    Coordinate[] coordinateArray = Stream.of(coordinateSequence.toCoordinateArray())
        .map(this::transformCoordinate)
        .toArray(Coordinate[]::new);
    return new CoordinateArraySequence(coordinateArray);
  }

  private Coordinate transformCoordinate(Coordinate coordinate) {
    ProjCoordinate c1 = new ProjCoordinate(coordinate.x, coordinate.y);
    ProjCoordinate c2 = coordinateTransform.transform(c1, new ProjCoordinate());
    return new Coordinate(c2.x, c2.y);
  }

  @Override
  public void match(Node node) {
    handleElement(node);
  }

  @Override
  public void match(Way way) {
    handleElement(way);
  }

  @Override
  public void match(Relation relation) {
    handleElement(relation);
  }

  private void handleElement(Element element) {
    if (element.getGeometry() != null) {
      Geometry geometry = transform(element.getGeometry());
      geometry.setSRID(dstSRID);
      element.setGeometry(geometry);
    }
  }

}
