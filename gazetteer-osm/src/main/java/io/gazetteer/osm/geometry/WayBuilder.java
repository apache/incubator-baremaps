package io.gazetteer.osm.geometry;

import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.store.StoreReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.proj4j.CoordinateTransform;

/**
 * A {@code WayBuilder} builds JTS linestring or polygons from OSM ways.
 */
public class WayBuilder extends GeometryBuilder<Way> {

  private final GeometryFactory geometryFactory;

  private final StoreReader<Long, Coordinate> coordinateStore;

  /**
   * Constructs a {code WayBuilder}.
   *
   * @param coordinateTransform the {@code CoordinateTransform} used to project OSM coordinates
   * @param geometryFactory     the {@code GeometryFactory} used to create polygons and multipolygons
   * @param coordinateStore     the {@code StoreReader} used to retrieve the coordinates of a node
   */
  public WayBuilder(
      CoordinateTransform coordinateTransform,
      GeometryFactory geometryFactory,
      StoreReader<Long, Coordinate> coordinateStore) {
    super(coordinateTransform);
    this.geometryFactory = geometryFactory;
    this.coordinateStore = coordinateStore;
  }

  /**
   * Builds a JTS linestring or polygons from OSM way.
   *
   * @param entity an OSM way
   * @return a JTS linestring or polygons corresponding to the way
   */
  public Geometry build(Way entity) {
    Coordinate[] coordinates =
        coordinateStore.getAll(entity.getNodes()).stream()
            .map(coordinate -> toCoordinate(coordinate.getX(), coordinate.getY()))
            .toArray(Coordinate[]::new);
    if (coordinates.length > 3 && coordinates[0].equals(coordinates[coordinates.length - 1])) {
      return geometryFactory.createPolygon(coordinates);
    } else if (coordinates.length > 1) {
      return geometryFactory.createLineString(coordinates);
    } else {
      return null;
    }
  }
}
