package io.gazetteer.osm.geometry;

import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.store.StoreReader;
import io.gazetteer.osm.stream.Try;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.proj4j.CoordinateTransform;

/**
 * A {@code RelationBuilder} builds JTS polygons and multipolygons from OSM relations.
 */
public class RelationBuilder extends GeometryBuilder<Relation> {

  private final GeometryFactory geometryFactory;

  private final StoreReader<Long, Coordinate> coordinateStore;

  private final StoreReader<Long, List<Long>> referenceStore;

  /**
   * Constructs a {@code RelationBuilder}.
   * @param coordinateTransform  the {@code CoordinateTransform} used to project OSM coordinates.
   * @param geometryFactory the {@code GeometryFactory} used to create polygons and multipolygons
   * @param coordinateStore the {@code StoreReader} used to retrieve the coordinates of a node
   * @param referenceStore the {@code StoreReader} used to retrieve the nodes of a way
   */
  public RelationBuilder(CoordinateTransform coordinateTransform, GeometryFactory geometryFactory,
      StoreReader<Long, Coordinate> coordinateStore, StoreReader<Long, List<Long>> referenceStore) {
    super(coordinateTransform);
    this.geometryFactory = geometryFactory;
    this.coordinateStore = coordinateStore;
    this.referenceStore = referenceStore;
  }

  /**
   * Builds a JTS geometry from an OSM relation
   * @param entity an OSM relation
   * @return a JTS polygon or multipolygon corresponding to the relation
   */
  public Geometry build(Relation entity) {
    // Check whether the relation is a multipolygon
    Map<String, String> tags = entity.getInfo().getTags();
    if (!"multipolygon".equals(tags.get("type"))) {
      return null;
    }

    // Collect the members of the relation
    List<LineString> members = entity.getMembers()
        .stream()
        .map(member -> referenceStore.get(member.getRef()))
        .filter(reference -> reference != null)
        .map(reference -> Try.of(() -> coordinateStore.getAll(reference).stream()
            .filter(coordinate -> coordinate != null)
            .map(coordinate -> toCoordinate(coordinate.getX(), coordinate.getY()))
            .toArray(Coordinate[]::new)))
        .filter(t -> t.isSuccess())
        .map(t -> t.value())
        .filter(t -> t.length > 3 && t[0].equals(t[t.length - 1]))
        .map(t -> geometryFactory.createLineString(t))
        .collect(Collectors.toList());

    // Check whether the relation contains members
    if (members.isEmpty()) {
      return  null;
    }

    // Create the polygon from the members
    Polygonizer polygonizer = new Polygonizer(true);
    polygonizer.add(members);
    return polygonizer.getGeometry();
  }

}
