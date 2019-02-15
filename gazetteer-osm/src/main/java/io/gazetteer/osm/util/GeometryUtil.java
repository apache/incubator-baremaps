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

  public static Coordinate coordinate(double x, double y) {
    CoordinateTransform ct = ctFactory.createTransform(epsg4326, epsg3857);
    ProjCoordinate coordinate =
            ct.transform(new ProjCoordinate(x, y), new ProjCoordinate());
    return new Coordinate(coordinate.x, coordinate.y);
  }

  public static Geometry asGeometry(Node node) {
    Coordinate coordinate = coordinate(node.getLon(), node.getLat());
    return new GeometryFactory(new PrecisionModel(), 3857).createPoint(coordinate);
  }

  public static Geometry asGeometry(Way way, DataStore<Long, Node> nodeStore)
      throws DataStoreException {
    GeometryFactory geometryFactory = new GeometryFactory();
    List<Long> ids = way.getNodes();
    List<Node> nodes = nodeStore.getAll(ids);
    List<Coordinate> list = new ArrayList<>();
    for (Node node : nodes) {
      Coordinate coordinate = coordinate(node.getLon(), node.getLat());
      list.add(coordinate);
    }
    Coordinate[] coordinates = list.toArray(new Coordinate[0]);
    if (ids.get(0).equals(ids.get(ids.size() - 1)) && ids.size() > 3) {
      return geometryFactory.createPolygon(coordinates);
    } else if (ids.size() > 1) {
      return geometryFactory.createLineString(coordinates);
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static Geometry asGeometryWithWrappedException(
      Way way, DataStore<Long, Node> nodeEntityStore) {
    try {
      return GeometryUtil.asGeometry(way, nodeEntityStore);
    } catch (DataStoreException e) {
      throw new WrappedException(e);
    }
  }

  public static Geometry asGeometry(Relation relation) {
    throw new UnsupportedOperationException();
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
