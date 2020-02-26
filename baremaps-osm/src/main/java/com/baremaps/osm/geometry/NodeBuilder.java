package com.baremaps.osm.geometry;

import com.baremaps.osm.model.Node;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.proj4j.CoordinateTransform;

/** A {@code NodeBuilder} builds JTS points from OSM nodes. */
public class NodeBuilder extends GeometryBuilder<Node> {

  protected final GeometryFactory geometryFactory;

  /**
   * Constructs a {@code NodeBuilder}.
   *
   * @param coordinateTransform the {@code CoordinateTransform} used to project OSM coordinates
   * @param geometryFactory the {@code GeometryFactory} used to build OSM points
   */
  public NodeBuilder(CoordinateTransform coordinateTransform, GeometryFactory geometryFactory) {
    super(coordinateTransform);
    this.geometryFactory = geometryFactory;
  }

  /**
   * Builds a JTS point from a OSM node.
   *
   * @param entity an OSM node
   * @return a JTS point corresponding to the node
   */
  public Point build(Node entity) {
    Coordinate coordinate = toCoordinate(entity.getLon(), entity.getLat());
    return geometryFactory.createPoint(coordinate);
  }
}
