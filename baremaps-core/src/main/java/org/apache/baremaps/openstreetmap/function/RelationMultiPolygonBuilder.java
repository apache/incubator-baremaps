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

import static org.apache.baremaps.utils.GeometryUtils.GEOMETRY_FACTORY_WGS84;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.openstreetmap.model.Member;
import org.apache.baremaps.openstreetmap.model.Member.MemberType;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.stream.StreamException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer that builds and sets a relation geometry via side effects.
 */
public class RelationMultiPolygonBuilder implements Consumer<Relation> {

  private static final Logger logger = LoggerFactory.getLogger(RelationMultiPolygonBuilder.class);

  private final DataMap<Long, Coordinate> coordinateMap;
  private final DataMap<Long, List<Long>> referenceMap;

  /**
   * Constructs a relation geometry builder.
   *
   * @param coordinateMap the coordinates map
   * @param referenceMap the references map
   */
  public RelationMultiPolygonBuilder(DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap) {
    this.coordinateMap = coordinateMap;
    this.referenceMap = referenceMap;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Relation relation) {
    try {
      var start = System.currentTimeMillis();

      var tags = relation.getTags();

      // Filter out type that are not multipolygon or boundary geometries
      // TODO: filter out at the stream level
      if (!("multipolygon".equals(tags.get("type"))
          || "boundary".equals(tags.get("type")))) {
        return;
      }

      // Filter coastline geometries
      // TODO: filter out at the stream level
      if ("coastline".equals(tags.get("natural"))) {
        return;
      }

      // Categorize the members of the relation by their role
      var outerMembers = new ArrayList<Member>();
      var innerMembers = new ArrayList<Member>();
      var otherMembers = new ArrayList<Member>();
      for (var member : relation.getMembers()) {
        if (MemberType.WAY.equals(member.getType())) {
          switch (member.getRole()) {
            case "outer" -> outerMembers.add(member);
            case "inner" -> innerMembers.add(member);
            default -> otherMembers.add(member);
          }
        }
      }

      // Prepare the outer polygons
      var outerPolygons = createPolygons(outerMembers);

      // Prepare the inner polygons
      var innerPolygons = createPolygons(innerMembers);
      innerPolygons = combinePolygons(innerPolygons);

      // Prepare the other polygons
      var otherPolygons = createPolygons(otherMembers);
      otherPolygons = combinePolygons(otherPolygons);

      // Categorize the other polygons as inner or outer
      for (var otherPolygon : otherPolygons) {

        // If the outer polygon contains the other polygon, it is an inner polygon
        for (var outerPolygon : outerPolygons) {
          if (outerPolygon.contains(otherPolygon)) {
            innerPolygons.add(otherPolygon);
          }
        }

        // Otherwise, it is an outer polygon
        if (!innerPolygons.contains(otherPolygon)) {
          outerPolygons.add(otherPolygon);
        }
      }

      // Merge the outer and inner polygons to build the relation geometry
      var polygons = new ArrayList<Polygon>();

      // Iterate over the outer polygons
      for (var outerPolygon : outerPolygons) {

        // Initialize the shell and holes of the polygon
        var shell = outerPolygon.getExteriorRing();
        var holes = new ArrayList<LinearRing>();
        for (int i = 0; i < outerPolygon.getNumInteriorRing(); i++) {
          holes.add(outerPolygon.getInteriorRingN(i));
        }

        // Use a prepared geometry to speed up the contains operation
        var preparedOuterPolygon = PreparedGeometryFactory.prepare(outerPolygon);

        // Find to which inner polygon the outer polygon belongs
        for (var innerPolygon : innerPolygons) {
          if (preparedOuterPolygon.contains(innerPolygon)) {

            // The exterior ring of the inner polygon is a hole in the outer polygon
            holes.add(innerPolygon.getExteriorRing());

            // The interior rings of the inner polygon are in fact additional polygons
            for (int i = 0; i < innerPolygon.getNumInteriorRing(); i++) {
              var innerPolygonHole =
                  GEOMETRY_FACTORY_WGS84.createPolygon(innerPolygon.getInteriorRingN(i));
              accumulatePolygons(innerPolygonHole, polygons);
            }
          }
        }

        // Build the polygon from the shell and holes
        var polygon = GEOMETRY_FACTORY_WGS84.createPolygon(shell, holes.toArray(new LinearRing[0]));
        accumulatePolygons(polygon, polygons);
      }

      // Build the multipolygon from the polygons
      if (!polygons.isEmpty()) {
        var multiPolygon =
            GEOMETRY_FACTORY_WGS84.createMultiPolygon(polygons.toArray(new Polygon[0]));
        relation.setGeometry(multiPolygon);
      }

      var end = System.currentTimeMillis();
      var duration = end - start;
      if (duration > 60 * 1000) {
        logger.debug("Relation #{} processed in {} ms", relation.getId(), duration);
      }
    } catch (Exception e) {
      logger.debug("Unable to build the geometry for relation #" + relation.getId(), e);
      relation.setGeometry(GEOMETRY_FACTORY_WGS84.createEmpty(0));
    }
  }

  private List<Polygon> createPolygons(List<Member> members) {
    var polygons = new ArrayList<Polygon>();
    var lineMerger = new LineMerger();

    // A member can either be a polygon or a line part of a polygon
    for (Member member : members) {
      var lineString = createLineString(member);
      if (lineString.isClosed()) {
        var polygon = GEOMETRY_FACTORY_WGS84.createPolygon(lineString.getCoordinateSequence());
        accumulatePolygons(polygon, polygons);
      } else {
        lineMerger.add(lineString);
      }
    }

    // Merge the lines to build the polygons
    for (Object geometry : lineMerger.getMergedLineStrings()) {
      if (geometry instanceof LineString lineString) {
        if (lineString.isClosed()) {
          var polygon = GEOMETRY_FACTORY_WGS84.createPolygon(lineString.getCoordinates());
          accumulatePolygons(polygon, polygons);
        }
      }
    }

    return polygons;
  }

  private List<Polygon> combinePolygons(List<Polygon> polygons) {
    var geometry = GEOMETRY_FACTORY_WGS84.createEmpty(0);
    for (Polygon polygon : polygons) {
      geometry = geometry.symDifference(polygon);
    }
    var combinedPolygons = new ArrayList<Polygon>();
    for (Object polygon : PolygonExtracter.getPolygons(geometry)) {
      combinedPolygons.add((Polygon) polygon);
    }
    return combinedPolygons;
  }

  private void accumulatePolygons(Polygon polygon, List<Polygon> accumulator) {
    if (polygon.isValid()) {
      accumulator.add(polygon);
    } else {
      var geometryFixer = new GeometryFixer(polygon);
      var fixedGeometry = geometryFixer.getResult();
      if (fixedGeometry instanceof Polygon fixedPolygon) {
        accumulator.add(fixedPolygon);
      } else if (fixedGeometry instanceof MultiPolygon fixedMultiPolygon) {
        PolygonExtracter.getPolygons(fixedMultiPolygon, accumulator);
      }
    }
  }

  private LineString createLineString(Member member) {
    try {
      List<Long> refs = referenceMap.get(member.getRef());

      // Build the coordinate list and remove duplicates.
      List<Coordinate> list = new ArrayList<>();
      Coordinate previous = null;
      for (Long id : refs) {
        Coordinate coordinate = coordinateMap.get(id);

        // remove duplicate coordinates
        if (coordinate != null && !coordinate.equals(previous)) {
          list.add(coordinate);
          previous = coordinate;
        }
      }

      Coordinate[] array = list.toArray(new Coordinate[0]);
      return GEOMETRY_FACTORY_WGS84.createLineString(array);
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }
}
