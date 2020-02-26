package com.baremaps.osm.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

/**
 * A {@code GeometryBuilder} is a base class for building JTS geometries from OSM entities.
 *
 * @param <T> the type of the entity to build geometries from.
 */
public abstract class GeometryBuilder<T> {

  private final CoordinateTransform coordinateTransform;

  /**
   * Constructs a {@code GeometryBuilder}.
   *
   * @param coordinateTransform the {@code CoordinateTransform} used to project OSM coordinates
   */
  public GeometryBuilder(CoordinateTransform coordinateTransform) {
    this.coordinateTransform = coordinateTransform;
  }

  protected Coordinate toCoordinate(double x, double y) {
    ProjCoordinate coordinate = coordinateTransform.transform(new ProjCoordinate(x, y), new ProjCoordinate());
    return new Coordinate(coordinate.x, coordinate.y);
  }

  /**
   * Builds a JTS geometry from an OSM entity.
   *
   * @param entity an entity
   * @return a geometry corresponding to the entity
   */
  public abstract Geometry build(T entity);
}
