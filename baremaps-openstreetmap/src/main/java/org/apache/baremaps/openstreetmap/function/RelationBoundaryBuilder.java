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

package org.apache.baremaps.openstreetmap.function;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationBoundaryBuilder implements Consumer<Entity> {

  private static final Logger logger = LoggerFactory.getLogger(RelationBoundaryBuilder.class);

  private final Map<Long, Coordinate> coordinateMap;
  private final Map<Long, List<Long>> referenceMap;

  public RelationBoundaryBuilder(Map<Long, Coordinate> coordinateMap,
      Map<Long, List<Long>> referenceMap) {
    this.coordinateMap = coordinateMap;
    this.referenceMap = referenceMap;
  }

  @Override
  public void accept(final Entity entity) {
    if (!(entity instanceof Relation relation)) {
      return;
    }
    if (!relation.getTags().containsKey("boundary")) {
      return;
    }
    try {
      long start = System.currentTimeMillis();
      buildBoundary(relation);
      long end = System.currentTimeMillis();
      long duration = end - start;
      if (duration > 60 * 1000) {
        logger.debug("Relation #{} processed in {} ms", relation.getId(), duration);
      }
    } catch (Exception e) {
      logger.error("Error processing relation #" + relation.getId(), e);
    }
  }

  public void buildBoundary(Relation relation) {
    List<Geometry> geometries = relation.getMembers().stream()
        .map(member -> switch (member.type()) {
          case NODE -> {
            Coordinate coord = coordinateMap.get(member.ref());
            yield (coord != null) ? GeometryUtils.GEOMETRY_FACTORY_WGS84.createPoint(coord) : null;
          }
          case WAY -> {
            List<Long> nodeIds = referenceMap.get(member.ref());
            if (nodeIds == null || nodeIds.isEmpty()) {
              yield null;
            }
            Coordinate[] coords = nodeIds.stream()
                .map(coordinateMap::get)
                .filter(Objects::nonNull)
                .toArray(Coordinate[]::new);
            if (coords.length < 2) {
              yield null;
            }
            if (coords[0].equals2D(coords[coords.length - 1]) && coords.length >= 4) {
              LinearRing ring = GeometryUtils.GEOMETRY_FACTORY_WGS84.createLinearRing(coords);
              yield GeometryUtils.GEOMETRY_FACTORY_WGS84.createPolygon(ring);
            } else {
              yield GeometryUtils.GEOMETRY_FACTORY_WGS84.createLineString(coords);
            }
          }
          case RELATION -> null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    Geometry finalGeometry;
    if (geometries.isEmpty()) {
      finalGeometry = GeometryUtils.GEOMETRY_FACTORY_WGS84.createGeometryCollection(new Geometry[]{});
    } else {
      Geometry combinedGeometry = GeometryCombiner.combine(geometries);
      if (combinedGeometry instanceof Polygon) {
        finalGeometry = GeometryUtils.GEOMETRY_FACTORY_WGS84.createMultiPolygon(new Polygon[]{(Polygon) combinedGeometry});
      } else if (combinedGeometry instanceof LineString) {
        finalGeometry = GeometryUtils.GEOMETRY_FACTORY_WGS84.createMultiLineString(new LineString[]{(LineString) combinedGeometry});
      } else if (combinedGeometry instanceof GeometryCollection && combinedGeometry.getNumGeometries() == 1) {
        Geometry single = combinedGeometry.getGeometryN(0);
        if (single instanceof Polygon) {
            finalGeometry = GeometryUtils.GEOMETRY_FACTORY_WGS84.createMultiPolygon(new Polygon[]{(Polygon) single});
        } else if (single instanceof LineString) {
            finalGeometry = GeometryUtils.GEOMETRY_FACTORY_WGS84.createMultiLineString(new LineString[]{(LineString) single});
        } else {
          finalGeometry = combinedGeometry;
        }
      } else if (combinedGeometry instanceof MultiPolygon || combinedGeometry instanceof MultiLineString) {
        finalGeometry = combinedGeometry;
      } else {
        finalGeometry = GeometryUtils.GEOMETRY_FACTORY_WGS84.createGeometryCollection(new Geometry[]{combinedGeometry});
      }
    }
    relation.setGeometry(finalGeometry);
  }
}
