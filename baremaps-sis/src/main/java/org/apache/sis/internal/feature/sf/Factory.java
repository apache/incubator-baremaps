package org.apache.sis.internal.feature.sf;

import java.io.ObjectStreamException;
import java.nio.ByteBuffer;
import mil.nga.sf.Geometry;
import org.apache.sis.internal.feature.Geometries;
import org.apache.sis.internal.feature.GeometryType;
import org.apache.sis.internal.feature.GeometryWrapper;
import org.apache.sis.math.Vector;
import org.apache.sis.setup.GeometryLibrary;

public class Factory extends Geometries<Geometry> {

  /**
   * Creates a new adapter for the given root geometry class.
   *
   * @param library       the enumeration value that identifies which geometry library is used.
   * @param rootClass     the root geometry class.
   * @param pointClass    the class for points.
   * @param polylineClass the class for polylines.
   * @param polygonClass  the class for polygons.
   */
  protected Factory(GeometryLibrary library, Class<Geometry> rootClass,
      Class<?> pointClass, Class<? extends Geometry> polylineClass,
      Class<? extends Geometry> polygonClass) {
    super(library, rootClass, pointClass, polylineClass, polygonClass);
  }

  @Override
  public GeometryWrapper<Geometry> castOrWrap(Object geometry) {
    return null;
  }

  @Override
  public GeometryWrapper<Geometry> parseWKT(String wkt) throws Exception {
    return null;
  }

  @Override
  public GeometryWrapper<Geometry> parseWKB(ByteBuffer data) throws Exception {
    return null;
  }

  @Override
  public Object createPoint(double x, double y) {
    return null;
  }

  @Override
  public Object createPoint(double x, double y, double z) {
    return null;
  }

  @Override
  public Geometry createPolyline(boolean polygon, int dimension, Vector... coordinates) {
    return null;
  }

  @Override
  public GeometryWrapper<Geometry> createMultiPolygon(Object[] geometries) {
    return null;
  }

  @Override
  public GeometryWrapper<Geometry> createFromComponents(GeometryType type, Object components) {
    return null;
  }

  @Override
  protected GeometryWrapper<Geometry> createWrapper(Geometry geometry) {
    return null;
  }

  @Override
  protected Object readResolve() throws ObjectStreamException {
    return null;
  }
}
