package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.*;
import io.gazetteer.osm.util.WrappedException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.util.ArrayList;
import java.util.List;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

public class GeometryUtil {

  public static Geometry asGeometry(Node node) {
    return new GeometryFactory().createPoint(new Coordinate(node.getLon(), node.getLat()));
  }

  public static Geometry asGeometry(Way way, DataStore<Long, Node> nodeStore)
      throws DataStoreException {
    GeometryFactory geometryFactory = new GeometryFactory();
    List<Long> ids = way.getNodes();
    List<Node> nodes = nodeStore.getAll(ids);
    List<Coordinate> list = new ArrayList<>();
    for (Node node : nodes) {
      list.add(new Coordinate(node.getLon(), node.getLat()));
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
    WKBWriter writer = new WKBWriter(2, wkbNDR);
    return writer.write(geometry);
  }
}
