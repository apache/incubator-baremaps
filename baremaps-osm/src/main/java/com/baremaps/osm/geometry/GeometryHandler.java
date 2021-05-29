package com.baremaps.osm.geometry;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.domain.Member;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ElementHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.polygonize.Polygonizer;
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
      if (way.getNodes().size() > 0) {
        List<Coordinate> coordinates = coordinateCache.get(way.getNodes());
        Coordinate[] array = coordinates.toArray(new Coordinate[coordinates.size()]);
        LineString line = geometryFactory.createLineString(array);
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
    Map<String, String> tags = relation.getTags();

    // Filter multipolygon geometries
    if (!"multipolygon".equals(tags.get("type"))) {
      return;
    }

    // Filter coastline geometries
    if ("coastline".equals(tags.get("natural"))) {
      return;
    }

    LineMerger lineMerger = new LineMerger();
    Polygonizer polygonizer = new Polygonizer(true);
    ArrayList<LineString> lines = new ArrayList<>();

    // Load the lines in memory
    for (Member member : relation.getMembers()) {
      try {
        List<Long> references = referenceCache.get(member.getRef());
        List<Coordinate> coordinates = coordinateCache.get(references);
        Coordinate[] array = coordinates.toArray(new Coordinate[coordinates.size()]);
        LineString linestring = geometryFactory.createLineString(array);
        lines.add(linestring);
      } catch (Exception e) {
        logger.warn("Unable to build the geometry for member #" + member.getRef() + " of relation #" + relation.getId(),
            e);
      }
    }

    // Merge the open lines
    for (LineString line : lines) {
      if (!line.isClosed()) {
        lineMerger.add(line);
      }
    }

    // Polygonize the merged open lines
    for (Object geometry : lineMerger.getMergedLineStrings()) {
      LineString lineString = (LineString) geometry;
      if (lineString.isClosed()) {
        polygonizer.add(lineString);
      }
    }

    // Polygonize the closed linestring
    for (LineString line : lines) {
      if (line.isClosed()) {
        Polygon polygon = geometryFactory.createPolygon(line.getCoordinates());
        polygonizer.add(polygon);
      }
    }

    // Set the geometry
    Geometry geometry = polygonizer.getGeometry();
    if (!geometry.isEmpty()) {
      relation.setGeometry(geometry);
    }
  }
}
