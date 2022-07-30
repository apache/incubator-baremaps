package com.baremaps.storage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public enum DataType {
  STRING(String.class),
  SHORT(Short.class),
  INTEGER(Integer.class),
  LONG(Long.class),
  FLOAT(Float.class),
  DOUBLE(Double.class),
  GEOMETRY(Geometry.class),
  POINT(Point.class),
  MULTIPOINT(MultiPoint.class),
  LINESTRING(LineString.class),
  MULTILINESTRING(MultiLineString.class),
  POLYGON(Polygon.class),
  MULTIPOLYGON(MultiPolygon.class),
  LINEARRING(LinearRing.class),
  GEOMETRYCOLLECTION(GeometryCollection.class),
  LOCALDATE(LocalDate.class),
  LOCALTIME(LocalTime.class),
  LOCALDATETIME(LocalDateTime.class);

  Class type;

  DataType(Class type) {
    this.type = type;
  }

  private static Map<Class, DataType> lookup = Arrays.stream(DataType.values())
      .collect(Collectors.toMap(dataType -> dataType.type, dataType -> dataType));

  public static boolean exists(Class type) {
    return lookup.containsKey(type);
  }

  public static DataType get(Class type) {
    return lookup.get(type);
  }

}
