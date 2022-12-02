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

package org.apache.baremaps.openstreetmap.function;

import static org.apache.baremaps.openstreetmap.utils.GeometryUtils.GEOMETRY_FACTORY_WGS84;

import java.util.*;
import java.util.function.Consumer;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.model.Member;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.stream.StreamException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer that builds and sets a relation geometry via side effects.
 */
public class RelationGeometryBuilder implements Consumer<Relation> {

  private static final Logger logger = LoggerFactory.getLogger(RelationGeometryBuilder.class);

  private final LongDataMap<Coordinate> coordinateMap;
  private final LongDataMap<List<Long>> referenceMap;

  /**
   * Constructs a relation geometry builder.
   *
   * @param coordinateMap the coordinates map
   * @param referenceMap the references map
   */
  public RelationGeometryBuilder(LongDataMap<Coordinate> coordinateMap,
      LongDataMap<List<Long>> referenceMap) {
    this.coordinateMap = coordinateMap;
    this.referenceMap = referenceMap;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Relation relation) {
    try {
      Map<String, String> tags = relation.getTags();

      // Filter multipolygon geometries
      if (!"multipolygon".equals(tags.get("type"))) {
        return;
      }

      // Filter coastline geometries
      if ("coastline".equals(tags.get("natural"))) {
        return;
      }

      // Prepare outer and inner polygons
      Set<Polygon> outerPolygons = createPolygons(relation, "outer");
      Set<Polygon> innerPolygons = createPolygons(relation, "inner");

      // Merge touching or overlapping inner polygons
      innerPolygons = mergeInnerPolygons(innerPolygons);

      // Do the line work
      List<Polygon> polygons = mergeOuterAndInnerPolygons(outerPolygons, innerPolygons);

      // Set the geometry of the relation
      if (polygons.size() == 1) {
        Polygon polygon = polygons.get(0);
        relation.setGeometry(polygon);
      } else if (polygons.size() > 1) {
        MultiPolygon multiPolygon =
            GEOMETRY_FACTORY_WGS84.createMultiPolygon(polygons.toArray(new Polygon[0]));
        relation.setGeometry(multiPolygon);
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for relation #" + relation.getId(), e);
    }
  }

  private List<Polygon> mergeOuterAndInnerPolygons(Set<Polygon> outerPolygons,
      Set<Polygon> innerPolygons) {
    List<Polygon> polygons = new ArrayList<>();
    for (Polygon outerPolygon : outerPolygons) {
      LinearRing shell = outerPolygon.getExteriorRing();
      List<LinearRing> holes = new ArrayList<>();
      PreparedGeometry prepared = PreparedGeometryFactory.prepare(outerPolygon);
      Iterator<Polygon> it = innerPolygons.iterator();
      while (it.hasNext()) {
        Polygon innerPolygon = it.next();
        if (prepared.containsProperly(innerPolygon)) {
          holes.add(innerPolygon.getExteriorRing());
          it.remove();
        }
      }
      Polygon polygon =
          GEOMETRY_FACTORY_WGS84.createPolygon(shell, holes.toArray(new LinearRing[0]));
      polygons.add(polygon);
    }
    return polygons;
  }

  private Set<Polygon> mergeInnerPolygons(Set<Polygon> innerPolygons) {
    Set<Polygon> usedPolygons = new HashSet<>();
    Set<Polygon> mergedPolygons = new HashSet<>();
    for (Polygon p1 : innerPolygons) {
      if (!usedPolygons.contains(p1)) {
        Set<Polygon> unionPolygons = new HashSet<>();
        unionPolygons.add(p1);
        for (Polygon p2 : innerPolygons) {
          if (!p1.equals(p2) && (p1.touches(p2) || p1.overlaps(p2))) {
            unionPolygons.add(p2);
            usedPolygons.add(p2);
          }
        }
        Geometry union = CascadedPolygonUnion.union(unionPolygons);
        for (Object polygon : PolygonExtracter.getPolygons(union)) {
          mergedPolygons.add((Polygon) polygon);
        }
      }
    }
    return mergedPolygons;
  }

  private Set<Polygon> createPolygons(Relation relation, String role) {
    Set<Polygon> polygons = new HashSet<>();
    LineMerger lineMerger = new LineMerger();
    relation.getMembers().stream().filter(m -> Member.MemberType.WAY.equals(m.getType()))
        .filter(m -> role.equals(m.getRole())).forEach(member -> {
          LineString line = createLine(member);
          if (line.isClosed()) {
            Polygon polygon = GEOMETRY_FACTORY_WGS84.createPolygon(line.getCoordinates());
            polygons.add(polygon);
          } else {
            lineMerger.add(line);
          }
        });
    lineMerger.getMergedLineStrings().stream().forEach(geometry -> {
      LineString line = (LineString) geometry;
      if (line.isClosed()) {
        Polygon polygon = GEOMETRY_FACTORY_WGS84.createPolygon(line.getCoordinates());
        polygons.add(polygon);
      }
    });
    return polygons;
  }

  private LineString createLine(Member member) {
    try {
      List<Long> refs = referenceMap.get(member.getRef());
      List<Coordinate> coords = refs.stream().map(coordinateMap::get).toList();
      Coordinate[] array = coords.toArray(new Coordinate[coords.size()]);
      return GEOMETRY_FACTORY_WGS84.createLineString(array);
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }
}
