package io.gazetteer.osm.geometry;

import io.gazetteer.common.postgis.GeometryUtils;
import io.gazetteer.osm.lmdb.LmdbStore;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.model.Way;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class WayGeometryBuilder {

  private final GeometryFactory geometryFactory;

  private final Store<Long, Coordinate> coordinateStore;

  public WayGeometryBuilder(GeometryFactory geometryFactory, Store<Long, Coordinate> coordinateStore) {
    this.geometryFactory = geometryFactory;
    this.coordinateStore = coordinateStore;
  }

  public Geometry create(Way entity) {
    Coordinate[] coordinates = coordinateStore.getAll(entity.getNodes())
        .stream()
        .map(coordinate -> GeometryUtils.toCoordinate(coordinate.getX(), coordinate.getY()))
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
