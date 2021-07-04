package com.baremaps.osm.geometry;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.domain.Member;
import com.baremaps.osm.domain.Member.MemberType;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.EntityConsumerAdapter;
import com.baremaps.stream.StreamException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

/**
 * Sets the geometry of an element via side-effects.
 */
public class CreateGeometryConsumer implements EntityConsumerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(CreateGeometryConsumer.class);

  protected final GeometryFactory geometryFactory;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public CreateGeometryConsumer(
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void match(Node node) {
    Point point = geometryFactory.createPoint(new Coordinate(node.getLon(), node.getLat()));
    node.setGeometry(point);
  }

  @Override
  public void match(Way way) {
    try {
      List<Coordinate> coordinates = coordinateCache.get(way.getNodes());
      Coordinate[] array = coordinates.toArray(new Coordinate[coordinates.size()]);
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

      // Create and prepare outer polygons
      Stream<Member> outerMembers = relation.getMembers().stream()
          .filter(m -> MemberType.way.equals(m.getType()))
          .filter(m -> "outer".equals(m.getRole()));
      Set<Polygon> outerPolygons = polygons(outerMembers)
          .collect(Collectors.toSet());

      // Create inner polygons
      Stream<Member> innerMembers = relation.getMembers().stream()
          .filter(m -> MemberType.way.equals(m.getType()))
          .filter(m -> "inner".equals(m.getRole()));
      Set<Polygon> innerPolygons = polygons(innerMembers)
          .collect(Collectors.toSet());

      // Merge touching or overlapping inner polygons
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

      // Do the line work
      List<Polygon> polygons = new ArrayList<>();
      for (Polygon outerPolygon : outerPolygons) {
        LinearRing shell = outerPolygon.getExteriorRing();
        List<LinearRing> holes = new ArrayList<>();
        PreparedGeometry prepared = PreparedGeometryFactory.prepare(outerPolygon);
        Iterator<Polygon> it = mergedPolygons.iterator();
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

      // Set the geometry of the relation
      if (polygons.size() == 1) {
        Polygon polygon = polygons.get(0);
        relation.setGeometry(polygon);
      } else if (polygons.size() > 1) {
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[0]));
        relation.setGeometry(multiPolygon);
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for relation #" + relation.getId(), e);
    }
  }

  private Stream<Polygon> polygons(Stream<Member> members) {
    LineMerger lineMerger = new LineMerger();
    Stream.Builder<Polygon> polygons = Stream.builder();
    members.forEach(member -> {
      LineString line = line(member);
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
    return polygons.build();
  }

  private LineString line(Member member) {
    try {
      List<Long> references = referenceCache.get(member.getRef());
      List<Coordinate> coordinates = coordinateCache.get(references);
      Coordinate[] array = coordinates.toArray(new Coordinate[coordinates.size()]);
      return geometryFactory.createLineString(array);
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }
}
