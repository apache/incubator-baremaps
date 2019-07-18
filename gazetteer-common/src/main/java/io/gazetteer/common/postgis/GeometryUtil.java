package io.gazetteer.common.postgis;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public class GeometryUtil {

  private static final CRSFactory crsFactory = new CRSFactory();
  private static final CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
  private static final CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
  private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

  public static Coordinate toCoordinate(double lon, double lat) {
    CoordinateTransform ct = ctFactory.createTransform(epsg4326, epsg3857);
    ProjCoordinate coordinate = ct.transform(new ProjCoordinate(lon, lat), new ProjCoordinate());
    return new Coordinate(coordinate.x, coordinate.y);
  }

  public static Geometry toPoint(double lon, double lat) {
    Coordinate coordinate = toCoordinate(lon, lat);
    return new GeometryFactory(new PrecisionModel(), 3857).createPoint(coordinate);
  }

  public static Geometry toGeometry(byte[] wkb) {
    try {
      WKBReader reader = new WKBReader(new GeometryFactory());
      return reader.read(wkb);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static byte[] toWKB(Geometry geometry) {
    WKBWriter writer = new WKBWriter(2, wkbNDR, true);
    return writer.write(geometry);
  }
}
