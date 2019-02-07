package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

public class GeometryUtil {

  public static Geometry asGeometry(Node node) {
    return new GeometryFactory().createPoint(new Coordinate(node.getLon(), node.getLat()));
  }

  public static Geometry asGeometry(Way way, EntityStore<Node> nodeStore) throws EntityStoreException {
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
      return null;
    }
  }


  public static Geometry asGeometry(Relation relation) {
    throw new NotImplementedException();
  }

  public static byte[] asWKB(Geometry geometry) {
    WKBWriter writer = new WKBWriter(2, wkbNDR);
    return writer.write(geometry);
  }

  public static Geometry asGeometry(byte[] wkb) {
    try {
      WKBReader reader = new WKBReader(new GeometryFactory());
      return reader.read(wkb);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
