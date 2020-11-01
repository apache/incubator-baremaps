package com.baremaps.importer.geometry;

import com.baremaps.importer.cache.Cache;
import com.baremaps.importer.cache.CacheException;
import com.baremaps.osm.ElementHandler;
import com.baremaps.osm.model.Bounds;
import com.baremaps.osm.EntityHandler;
import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

public class GeometryBuilder implements ElementHandler {

  protected final GeometryFactory geometryFactory;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;

  public GeometryBuilder(
      GeometryFactory geometryFactory,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache) {
    this.geometryFactory = geometryFactory;
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
      Coordinate[] coords = coordinateCache.getAll(way.getNodes()).stream()
          .toArray(Coordinate[]::new);
      if (coords.length > 3 && coords[0].equals(coords[coords.length - 1])) {
        way.setGeometry(geometryFactory.createPolygon(coords));
      } else if (coords.length > 1) {
        way.setGeometry(geometryFactory.createLineString(coords));
      } else {
        return;
      }
    } catch (CacheException e) {
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

    // Collect the members of the relation
    List<LineString> members = relation.getMembers()
        .stream()
        .map(member -> {
          try {
            return referenceCache.get(member.getRef());
          } catch (CacheException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .map(reference -> {
          try {
            return coordinateCache.getAll(reference).stream()
                .filter(Objects::nonNull)
                .toArray(Coordinate[]::new);
          } catch (CacheException e) {
            return null;
          }
        })
        .filter(t -> t != null)
        .map(t -> geometryFactory.createLineString(t))
        .collect(Collectors.toList());

    // Check whether the relation contains members
    if (members.isEmpty()) {
      return;
    }

    // Try to create the polygon from the members
    try {
      Polygonizer polygonizer = new Polygonizer(true);
      polygonizer.add(members);
      relation.setGeometry(polygonizer.getGeometry());
    } catch (Exception ex) {
      return;
    }
  }
}
