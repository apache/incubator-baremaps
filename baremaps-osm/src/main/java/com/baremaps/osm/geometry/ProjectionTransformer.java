package com.baremaps.osm.geometry;

import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

/**
 * A transformer that reprojects geometries and sets the correct output SRIDs.
 */
public class ProjectionTransformer extends GeometryTransformer {

  private final CoordinateTransform coordinateTransform;

  /**
   * Creates a transformer that reprojects geometries with the provided SRIDs.
   *
   * @param inputSRID the input SRID
   * @param outputSRID the output SRID
   */
  public ProjectionTransformer(int inputSRID, int outputSRID) {
    this(GeometryUtils.coordinateTransform(inputSRID, outputSRID));
  }

  /**
   * Creates a transformer that reproject geometries with the provided coordinate transform.
   *
   * @param coordinateTransform the coordinateTransform
   */
  public ProjectionTransformer(CoordinateTransform coordinateTransform) {
    this.coordinateTransform = coordinateTransform;
  }

  @Override
  protected CoordinateSequence transformCoordinates(CoordinateSequence coordinateSequence, Geometry parent) {
    Coordinate[] coordinateArray =
        Stream.of(coordinateSequence.toCoordinateArray())
            .map(this::transformCoordinate)
            .toArray(Coordinate[]::new);
    return new CoordinateArraySequence(coordinateArray);
  }

  private Coordinate transformCoordinate(Coordinate coordinate) {
    ProjCoordinate c1 = new ProjCoordinate(coordinate.x, coordinate.y);
    ProjCoordinate c2 = coordinateTransform.transform(c1, new ProjCoordinate());
    return new Coordinate(c2.x, c2.y);
  }

  protected Geometry transformPoint(Point geom, Geometry parent) {
    return withTargetSRID(super.transformPoint(geom, parent));
  }

  protected Geometry transformMultiPoint(MultiPoint geom, Geometry parent) {
    return withTargetSRID(super.transformMultiPoint(geom, parent));
  }

  protected Geometry transformLinearRing(LinearRing geom, Geometry parent) {
    return withTargetSRID(super.transformLinearRing(geom, parent));
  }

  protected Geometry transformLineString(LineString geom, Geometry parent) {
    return withTargetSRID(super.transformLineString(geom, parent));
  }

  protected Geometry transformMultiLineString(MultiLineString geom, Geometry parent) {
    return withTargetSRID(super.transformMultiLineString(geom, parent));
  }

  protected Geometry transformPolygon(Polygon geom, Geometry parent) {
    return withTargetSRID(super.transformPolygon(geom, parent));
  }

  protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
    return withTargetSRID(transformMultiPolygon(geom, parent));
  }

  protected Geometry transformGeometryCollection(GeometryCollection geom, Geometry parent) {
    return withTargetSRID(transformGeometryCollection(geom, parent));
  }

  private Geometry withTargetSRID(Geometry outputGeom) {
    outputGeom.setSRID(coordinateTransform.getTargetCRS().getProjection().getEPSGCode());
    return outputGeom;
  }

}
