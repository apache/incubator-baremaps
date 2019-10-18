package io.gazetteer.osm.geometry;

import io.gazetteer.common.postgis.GeometryUtils;
import io.gazetteer.osm.cache.Cache;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Relation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class RelationGeometryBuilder {

  private final GeometryFactory geometryFactory;

  private final Cache<Coordinate> nodeCache;
  private final Cache<List<Long>> wayCache;

  public RelationGeometryBuilder(GeometryFactory geometryFactory, Cache<Coordinate> nodeCache, Cache<List<Long>> wayCache) {
    this.geometryFactory = geometryFactory;
    this.nodeCache = nodeCache;
    this.wayCache = wayCache;
  }

  public Geometry create(Relation entity) {
    Map<String, String> tags = entity.getInfo().getTags();
    Stream<Geometry> geometries = polygons(entity.getMembers().stream());
    Geometry g = geometryFactory.buildGeometry(geometries.collect(Collectors.toList()));
    return g;
  }

  public Stream<Geometry> polygons(Stream<Member> members) {
    return members.flatMap(member -> {
      switch (member.getType()) {
        case node:
          return Stream.of();
        case way:
          List<Long> references = wayCache.get(member.getRef());
          if (references == null) {
            return Stream.of();
          }
          Coordinate[] coordinates = references
              .stream()
              .map(r -> nodeCache.get(r))
              .filter(coordinate -> coordinate != null)
              .map(coordinate -> GeometryUtils.toCoordinate(coordinate.getX(), coordinate.getY()))
              .toArray(Coordinate[]::new);
          if (coordinates.length > 3 && coordinates[0].equals(coordinates[coordinates.length - 1])) {
            return Stream.of(geometryFactory.createPolygon(coordinates));
          } else {
            return Stream.of(geometryFactory.createLineString(coordinates));
          }
        default:
          return Stream.of();
      }
    });
  }

}
