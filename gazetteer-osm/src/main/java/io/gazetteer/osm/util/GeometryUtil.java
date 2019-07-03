package io.gazetteer.osm.util;

import io.gazetteer.osm.model.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.proj4j.*;

import java.util.ArrayList;
import java.util.List;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

public class GeometryUtil {

  private static final CRSFactory crsFactory = new CRSFactory();
  private static final CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
  private static final CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
  private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

  public static Coordinate coordinate(double lon, double lat) {
    CoordinateTransform ct = ctFactory.createTransform(epsg4326, epsg3857);
    ProjCoordinate coordinate = ct.transform(new ProjCoordinate(lon, lat), new ProjCoordinate());
    return new Coordinate(coordinate.x, coordinate.y);
  }

  public static Geometry point(double lon, double lat) {
    Coordinate coordinate = coordinate(lon, lat);
    return new GeometryFactory(new PrecisionModel(), 3857).createPoint(coordinate);
  }

  public static Geometry asGeometry(byte[] wkb) {
    try {
      WKBReader reader = new WKBReader(new GeometryFactory());
      return reader.read(wkb);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static byte[] asWKB(Geometry geometry) {
    WKBWriter writer = new WKBWriter(2, wkbNDR, true);
    return writer.write(geometry);
  }
}
