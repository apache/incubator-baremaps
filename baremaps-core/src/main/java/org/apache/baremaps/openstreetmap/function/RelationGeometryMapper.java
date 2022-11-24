package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.model.Member;
import org.apache.baremaps.openstreetmap.model.MemberType;
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

import java.util.*;
import java.util.function.Function;

/**
 * A function that adds a geometry to a relation.
 */
public record RelationGeometryMapper(Context context) implements Function<Relation, Relation> {

  private static final Logger logger = LoggerFactory.getLogger(RelationGeometryMapper.class);

  /** {@inheritDoc} */
  public Relation apply(Relation relation) {
    try {
      Map<String, String> tags = relation.tags();

      // Filter multipolygon geometries
      if (!"multipolygon".equals(tags.get("type"))) {
        return relation;
      }

      // Filter coastline geometries
      if ("coastline".equals(tags.get("natural"))) {
        return relation;
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
        return relation.withGeometry(polygon);
      } else if (polygons.size() > 1) {
        MultiPolygon multiPolygon =
          context.geometryFactory().createMultiPolygon(polygons.toArray(new Polygon[0]));
        return relation.withGeometry(multiPolygon);
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for relation #" + relation.id(), e);
    }

    return relation;
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
      Polygon polygon = context.geometryFactory().createPolygon(shell, holes.toArray(new LinearRing[0]));
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
    relation.members().stream().filter(m -> MemberType.WAY.equals(m.type()))
      .filter(m -> role.equals(m.role())).forEach(member -> {
        LineString line = createLine(member);
        if (line.isClosed()) {
          Polygon polygon = context.geometryFactory().createPolygon(line.getCoordinates());
          polygons.add(polygon);
        } else {
          lineMerger.add(line);
        }
      });
    lineMerger.getMergedLineStrings().stream().forEach(geometry -> {
      LineString line = (LineString) geometry;
      if (line.isClosed()) {
        Polygon polygon = context.geometryFactory().createPolygon(line.getCoordinates());
        polygons.add(polygon);
      }
    });
    return polygons;
  }

  private LineString createLine(Member member) {
    try {
      List<Long> refs = context.referenceMap().get(member.ref());
      List<Coordinate> coords = refs.stream().map(context.coordinateMap()::get).toList();
      Coordinate[] array = coords.toArray(new Coordinate[coords.size()]);
      return context.geometryFactory().createLineString(array);
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

}
