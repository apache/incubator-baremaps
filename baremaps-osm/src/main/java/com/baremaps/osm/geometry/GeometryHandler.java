package com.baremaps.osm.geometry;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheException;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ElementHandler;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeometryHandler implements ElementHandler {

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
      Coordinate[] coords = coordinateCache.get(way.getNodes())
          .stream()
          .toArray(Coordinate[]::new);
      if (coords.length > 3 && coords[0].equals(coords[coords.length - 1])) {
        way.setGeometry(geometryFactory.createPolygon(coords));
      } else if (coords.length > 1) {
        way.setGeometry(geometryFactory.createLineString(coords));
      } else {
        return;
      }
    } catch (Exception e) {
      logger.warn("Unable to build the geometry for way " + way.getId(), e);
      return;
    }
  }

  @Override
  public void handle(Relation relation) {
    // Check whether the relation is a multipolygon
    Map<String, String> tags = relation.getTags();
    if (!"multipolygon".equals(tags.get("type"))) {
      return;
    }

    // Collect the polygons of the relation
    Polygon[] members = relation.getMembers().stream()
        .filter(member -> "outer".equals(member.getRole()) || "inner".equals(member.getRole()))
        .map(member -> {
          try {
            return referenceCache.get(member.getRef());
          } catch (CacheException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .map(references -> {
          try {
            Coordinate[] coordinateArray = coordinateCache.get(references).stream()
                .filter(Objects::nonNull)
                .toArray(Coordinate[]::new);
            CoordinateList coordinateList = new CoordinateList(coordinateArray);
            coordinateList.closeRing();
            return geometryFactory.createPolygon(coordinateList.toCoordinateArray());
          } catch (CacheException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toArray(Polygon[]::new);

    if (members.length == 1) {
      relation.setGeometry(members[0]);
    }

    if (members.length > 1) {
      MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(members);
      relation.setGeometry(multiPolygon);
    }
  }
}
