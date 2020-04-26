/*
 * Copyright (C) 2011 The Baremaps Authors
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

import com.baremaps.osm.model.Node;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

/**
 * A {@code NodeBuilder} builds JTS points from OSM nodes.
 */
public class NodeBuilder extends GeometryBuilder<Node> {

  protected final CoordinateTransform coordinateTransform;
  protected final GeometryFactory geometryFactory;

  /**
   * Constructs a {@code NodeBuilder}.
   *
   * @param coordinateTransform the {@code CoordinateTransform} used to project OSM coordinates
   */
  public NodeBuilder(GeometryFactory geometryFactory, CoordinateTransform coordinateTransform) {
    this.coordinateTransform = coordinateTransform;
    this.geometryFactory = geometryFactory;
  }

  /**
   * Builds a JTS point from a OSM node.
   *
   * @param entity an OSM node
   * @return a JTS point corresponding to the node
   */
  public Point build(Node entity) {
    ProjCoordinate c1 = new ProjCoordinate(entity.getLon(), entity.getLat());
    ProjCoordinate c2 = coordinateTransform.transform(c1, new ProjCoordinate());
    return geometryFactory.createPoint(new Coordinate(c2.x, c2.y));
  }
}
