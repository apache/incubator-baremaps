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

package org.apache.baremaps.osm.function;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.osm.model.Member;
import org.apache.baremaps.osm.model.Member.MemberType;
import org.apache.baremaps.osm.model.Node;
import org.apache.baremaps.osm.model.Relation;
import org.apache.baremaps.osm.model.Way;
import org.apache.baremaps.stream.StreamException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A consumer that creates and sets the geometry of OpenStreetMap entities via side-effects. */
public class CreateGeometryConsumer implements EntityConsumerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(CreateGeometryConsumer.class);

  protected final GeometryFactory geometryFactory;
  private final LongDataMap<Coordinate> coordinates;
  private final LongDataMap<List<Long>> references;

  /**
   * Constructs a consumer that uses the provided caches to create and set geometries.
   *
   * @param coordinates the coordinate cache
   * @param references the reference cache
   */
  public CreateGeometryConsumer(LongDataMap<Coordinate> coordinates,
      LongDataMap<List<Long>> references) {
    this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    this.coordinates = coordinates;
    this.references = references;
  }

  /** {@inheritDoc} */
  @Override
  public void match(Node node) {
    Point point = geometryFactory.createPoint(new Coordinate(node.getLon(), node.getLat()));
    node.setGeometry(point);
  }

  /** {@inheritDoc} */
  @Override
  public void match(Way way) {
    try {
      List<Coordinate> list = way.getNodes().stream().map(coordinates::get).toList();
      Coordinate[] array = list.toArray(new Coordinate[list.size()]);
      LineString line = geometryFactory.createLineString(array);
      if (!line.isEmpty()) {
        if (!line.isClosed()) {
          way.setGeometry(line);
        } else {
          Polygon polygon = geometryFactory.createPolygon(line.getCoordinates());
          way.setGeometry(polygon);
        }
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for way #" + way.getId(), e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void match(Relation relation) {
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
            geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[0]));
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
      Polygon polygon = geometryFactory.createPolygon(shell, holes.toArray(new LinearRing[0]));
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
    relation.getMembers().stream().filter(m -> MemberType.WAY.equals(m.getType()))
        .filter(m -> role.equals(m.getRole())).forEach(member -> {
          LineString line = createLine(member);
          if (line.isClosed()) {
            Polygon polygon = geometryFactory.createPolygon(line.getCoordinates());
            polygons.add(polygon);
          } else {
            lineMerger.add(line);
          }
        });
    lineMerger.getMergedLineStrings().stream().forEach(geometry -> {
      LineString line = (LineString) geometry;
      if (line.isClosed()) {
        Polygon polygon = geometryFactory.createPolygon(line.getCoordinates());
        polygons.add(polygon);
      }
    });
    return polygons;
  }

  private LineString createLine(Member member) {
    try {
      List<Long> refs = this.references.get(member.getRef());
      List<Coordinate> coords = refs.stream().map(coordinates::get).toList();
      Coordinate[] array = coords.toArray(new Coordinate[coords.size()]);
      return geometryFactory.createLineString(array);
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }
}
