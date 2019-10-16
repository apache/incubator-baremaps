package io.gazetteer.osm.geometry;

import io.gazetteer.common.postgis.GeometryUtils;
import io.gazetteer.osm.model.Node;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class NodeGeometryBuilder {

  protected final GeometryFactory geometryFactory;

  public NodeGeometryBuilder(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public Point create(Node entity) {
    Coordinate coordinate = GeometryUtils.toCoordinate(entity.getLon(), entity.getLat());
    return geometryFactory.createPoint(coordinate);
  }
}
