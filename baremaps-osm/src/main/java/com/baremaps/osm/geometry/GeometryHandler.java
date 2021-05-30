package com.baremaps.osm.geometry;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.domain.Member;
import com.baremaps.osm.domain.Member.MemberType;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ElementHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeometryHandler implements ElementHandler {

  private static final ExecutorService executor = Executors.newWorkStealingPool();

  private static Logger logger = LoggerFactory.getLogger(GeometryHandler.class);

  protected final GeometryFactory geometryFactory;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public GeometryHandler(
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
  }

  @Override
  public void handle(Node node) {
    Point point = geometryFactory.createPoint(new Coordinate(node.getLon(), node.getLat()));
    node.setGeometry(point);
  }

  @Override
  public void handle(Way way) {
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
  public void handle(Relation relation) {
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

      Geometry polygon = geometryFactory.createPolygon();
      LineMerger lineMerger = new LineMerger();
      ArrayList<LineString> lines = new ArrayList<>();

      List<Member> members = relation.getMembers().stream()
          .filter(m -> MemberType.way.equals(m.getType()))
          .collect(Collectors.toList());

      // Load the lines in memory
      for (Member member : members) {
        List<Long> references = referenceCache.get(member.getRef());
        List<Coordinate> coordinates = coordinateCache.get(references);
        Coordinate[] array = coordinates.toArray(new Coordinate[coordinates.size()]);
        LineString linestring = geometryFactory.createLineString(array);
        lines.add(linestring);
      }

      // Merge the open lines
      for (LineString line : lines) {
        if (!line.isClosed()) {
          lineMerger.add(line);
        }
      }

      // Polygonize the merged open lines
      for (Object geometry : lineMerger.getMergedLineStrings()) {
        LineString line = (LineString) geometry;
        if (line.isClosed()) {
          Polygon p = geometryFactory.createPolygon(line.getCoordinates());
          polygon = polygon.symDifference(p);
        }
      }

      // Polygonize the closed linestring
      for (LineString line : lines) {
        if (line.isClosed()) {
          Polygon p = geometryFactory.createPolygon(line.getCoordinates());
          polygon = polygon.symDifference(p);
        }
      }

      // Set the geometry
      if (!polygon.isEmpty()) {
        relation.setGeometry(polygon);
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for relation #" + relation.getId(), e);
    }
  }
}
