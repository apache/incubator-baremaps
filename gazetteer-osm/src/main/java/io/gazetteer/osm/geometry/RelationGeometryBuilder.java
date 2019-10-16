package io.gazetteer.osm.geometry;

import com.google.common.collect.Streams;
import io.gazetteer.osm.data.FixedSizeObjectMap;
import io.gazetteer.osm.data.VariableSizeObjectMap;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Member.Type;
import io.gazetteer.osm.model.Relation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class RelationGeometryBuilder {

  private final GeometryFactory geometryFactory;

  private final FixedSizeObjectMap<Coordinate> coordinateMap;
  private final VariableSizeObjectMap<List<Long>> referencesMap;

  public RelationGeometryBuilder(GeometryFactory geometryFactory, FixedSizeObjectMap<Coordinate> coordinateMap,
      VariableSizeObjectMap<List<Long>> referencesMap) {
    this.geometryFactory = geometryFactory;
    this.coordinateMap = coordinateMap;
    this.referencesMap = referencesMap;
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
          List<Long> references = referencesMap.get(member.getRef());
          if (references == null) {
            return Stream.of();
          }
          Coordinate[] coordinates = references.stream().map(r -> coordinateMap.get(r)).filter(c -> c != null).toArray(Coordinate[]::new);
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
