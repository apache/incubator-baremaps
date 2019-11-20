package io.gazetteer.osm.geometry;

import io.gazetteer.common.postgis.GeometryUtils;
import io.gazetteer.common.stream.Try;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Member.Type;
import io.gazetteer.osm.model.Relation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

public class RelationGeometryBuilder {

  private final GeometryFactory geometryFactory;

  private final Store<Long, Coordinate> coordinateStore;
  private final Store<Long, List<Long>> referenceStore;

  public RelationGeometryBuilder(GeometryFactory geometryFactory, Store<Long, Coordinate> coordinateStore, Store<Long, List<Long>> referenceStore) {
    this.geometryFactory = geometryFactory;
    this.coordinateStore = coordinateStore;
    this.referenceStore = referenceStore;
  }

  public Geometry create(Relation entity) {
    Map<String, String> tags = entity.getInfo().getTags();
    if (!"multipolygon".equals(tags.get("type"))) {
      return geometryFactory.createGeometryCollection();
    }
    List<Member> members = entity.getMembers();
    Polygonizer polygonizer = new Polygonizer();
    members.stream()
        .filter(member -> member.getType().equals(Type.way) && member.getRole().equals("outer"))
        .map(member -> referenceStore.get(member.getRef()))
        .filter(way -> way != null)
        .map(way -> Try.of(() -> coordinateStore.getAll(way).stream()
            .filter(coordinate -> coordinate != null)
            .map(coordinate -> GeometryUtils.toCoordinate(coordinate.getX(), coordinate.getY()))
            .toArray(Coordinate[]::new)))
        .filter(t -> t.isSuccess())
        .map(t -> geometryFactory.createLineString(t.value()))
        .forEach(way -> polygonizer.add(way));
    members.stream()
        .filter(member -> member.getType().equals(Type.way) && member.getRole().equals("inner"))
        .map(member -> referenceStore.get(member.getRef()))
        .filter(way -> way != null)
        .map(way -> coordinateStore.getAll(way).stream()
            .filter(coordinate -> coordinate != null)
            .map(coordinate -> GeometryUtils.toCoordinate(coordinate.getX(), coordinate.getY()))
            .toArray(Coordinate[]::new))
        .map(way -> geometryFactory.createLineString(way))
        .forEach(way -> polygonizer.add(way));
    Collection<Polygon> polygons = polygonizer.getPolygons();
    return geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[0])).union();
  }

}
