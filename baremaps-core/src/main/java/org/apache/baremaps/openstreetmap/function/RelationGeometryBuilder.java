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

import java.util.*;
import java.util.function.Consumer;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.openstreetmap.model.Member;
import org.apache.baremaps.openstreetmap.model.Member.MemberType;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.stream.StreamException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer that builds and sets a relation geometry via side effects.
 */
public class RelationGeometryBuilder implements Consumer<Relation> {

  private static final Logger logger = LoggerFactory.getLogger(RelationGeometryBuilder.class);

  private final DataMap<Long, Coordinate> coordinateMap;
  private final DataMap<Long, List<Long>> referenceMap;

  /**
   * Constructs a relation geometry builder.
   *
   * @param coordinateMap the coordinates map
   * @param referenceMap the references map
   */
  public RelationGeometryBuilder(DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap) {
    this.coordinateMap = coordinateMap;
    this.referenceMap = referenceMap;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Relation relation) {
    try {
      var start = System.currentTimeMillis();

      Map<String, Object> tags = relation.getTags();

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

      // Retain only the members that are ways and form polygons
      var wayMembers =
          relation.getMembers().stream().filter(m -> MemberType.WAY.equals(m.getType())).toList();

      // Prepare outer polygons
      var outerMembers = wayMembers.stream().filter(m -> m.getRole().equals("outer")).toList();
      var outerPolygons = createPolygons(outerMembers);

      // Prepare inner polygons
      var innerMembers = wayMembers.stream().filter(m -> m.getRole().equals("inner")).toList();
      var innerPolygons = createPolygons(innerMembers);

      // Union touching or overlapping inner polygons
      innerPolygons = combinePolygons(innerPolygons);

      // Prepare uncatgorized polygons
      var uncategorizedMembers = wayMembers.stream().filter(m -> m.getRole().equals("")).toList();
      var uncategorizedPolygons = createPolygons(uncategorizedMembers);

      // Difference touching or overlapping uncategorized polygons
      uncategorizedPolygons = combinePolygons(uncategorizedPolygons);

      // An uncategorized polygon is an inner polygon if it is contained by an outer polygon
      for (Polygon uncategorizedPolygon : uncategorizedPolygons) {
        for (Polygon outerPolygon : outerPolygons) {
          if (outerPolygon.contains(uncategorizedPolygon)) {
            innerPolygons.add(uncategorizedPolygon);
          }
        }
        if (!innerPolygons.contains(uncategorizedPolygon)) {
          outerPolygons.add(uncategorizedPolygon);
        }
      }



      // Do the line work
      List<Polygon> polygons = mergeOuterAndInnerPolygons(outerPolygons, innerPolygons);

      // Set the geometry of the relation
      if (polygons.size() == 1) {
        Geometry polygon = polygons.get(0);
        relation.setGeometry(polygon);
      } else if (polygons.size() > 1) {
        MultiPolygon multiPolygon =
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

  private List<Polygon> mergeOuterAndInnerPolygons(
      Set<Polygon> outerPolygons,
      Set<Polygon> innerPolygons) {
    List<Polygon> polygons = new ArrayList<>();
    for (Polygon outerPolygon : outerPolygons) {
      LinearRing shell = outerPolygon.getExteriorRing();
      List<LinearRing> holes = new ArrayList<>();
      for (int i = 0; i < outerPolygon.getNumInteriorRing(); i++) {
        holes.add(outerPolygon.getInteriorRingN(i));
      }
      PreparedGeometry prepared = PreparedGeometryFactory.prepare(outerPolygon);
      Iterator<Polygon> it = innerPolygons.iterator();
      while (it.hasNext()) {
        Polygon innerPolygon = it.next();
        if (prepared.contains(innerPolygon)) {
          holes.add(innerPolygon.getExteriorRing());
          for (int i = 0; i < innerPolygon.getNumInteriorRing(); i++) {
            var innerPolygonHole =
                GEOMETRY_FACTORY_WGS84.createPolygon(innerPolygon.getInteriorRingN(i));
            polygons.add(innerPolygonHole);
          }
          it.remove();
        }
      }
      Polygon polygon =
          GEOMETRY_FACTORY_WGS84.createPolygon(shell, holes.toArray(new LinearRing[0]));

      // Fix invalid polygons with a buffer (e.g. self-intersecting)
      if (polygon.isValid()) {
        polygons.add(polygon);
      } else {
        var geometryFixer = new GeometryFixer(polygon);
        var fixedGeometry = geometryFixer.getResult();
        if (fixedGeometry instanceof Polygon fixedPolygon) {
          polygons.add(fixedPolygon);
        } else if (fixedGeometry instanceof MultiPolygon fixedMultiPolygon) {
          PolygonExtracter.getPolygons(fixedMultiPolygon, polygons);
        }
      }
    }
    return polygons;
  }

  private Set<Polygon> combinePolygons(Set<Polygon> polygons) {

    var geometry = GEOMETRY_FACTORY_WGS84.createEmpty(0);

    for (Polygon polygon : polygons) {
      geometry = geometry.symDifference(polygon);
    }

    Set<Polygon> mergedPolygons = new HashSet<>();
    for (Object polygon : PolygonExtracter.getPolygons(geometry)) {
      mergedPolygons.add((Polygon) polygon);
    }

    return mergedPolygons;
  }

  private Set<Polygon> createPolygons(List<Member> members) {
    List<Polygon> polygons = new ArrayList<>();
    LineMerger lineMerger = new LineMerger();
    for (Member member : members) {
      LineString line = createLine(member);
      if (line.isClosed()) {
        var polygon = GEOMETRY_FACTORY_WGS84.createPolygon(line.getCoordinateSequence());
        if (polygon.isValid()) {
          polygons.add(polygon);
        } else {
          var geometryFixer = new GeometryFixer(polygon);
          var fixedGeometry = geometryFixer.getResult();
          if (fixedGeometry instanceof Polygon fixedPolygon) {
            polygons.add(fixedPolygon);
          } else if (fixedGeometry instanceof MultiPolygon fixedMultiPolygon) {
            PolygonExtracter.getPolygons(fixedMultiPolygon, polygons);
          }
        }
      } else {
        lineMerger.add(line);
      }
    }
    for (Object geometry : lineMerger.getMergedLineStrings()) {
      LineString line = (LineString) geometry;
      if (line.isClosed()) {
        Polygon polygon = GEOMETRY_FACTORY_WGS84.createPolygon(line.getCoordinates());
        if (polygon.isValid()) {
          polygons.add(polygon);
        } else {
          var geometryFixer = new GeometryFixer(polygon);
          var fixedGeometry = geometryFixer.getResult();
          if (fixedGeometry instanceof Polygon fixedPolygon) {
            polygons.add(fixedPolygon);
          } else if (fixedGeometry instanceof MultiPolygon fixedMultiPolygon) {
            PolygonExtracter.getPolygons(fixedMultiPolygon, polygons);
          }
        }
      }
    }
    return new HashSet<>(polygons);
  }

  private LineString createLine(Member member) {
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
