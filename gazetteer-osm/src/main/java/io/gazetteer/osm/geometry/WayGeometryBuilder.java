package io.gazetteer.osm.geometry;

import io.gazetteer.osm.data.FixedSizeObjectMap;
import io.gazetteer.osm.model.Way;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class WayGeometryBuilder {

  private final GeometryFactory geometryFactory;

  private final FixedSizeObjectMap<Coordinate> coordinateMap;

  public WayGeometryBuilder(GeometryFactory geometryFactory, FixedSizeObjectMap<Coordinate> coordinateMap) {
    this.geometryFactory = geometryFactory;
    this.coordinateMap = coordinateMap;
  }

  public Geometry create(Way entity) {
    Coordinate[] coordinates = entity.getNodes().stream().map(node -> coordinateMap.get(node)).toArray(Coordinate[]::new);
    if (coordinates.length > 3 && coordinates[0].equals(coordinates[coordinates.length - 1])) {
      return geometryFactory.createPolygon(coordinates);
    } else if (coordinates.length > 1) {
      return geometryFactory.createLineString(coordinates);
    } else {
      return null;
    }
  }

}
