package io.gazetteer.osm;

import io.gazetteer.osm.postgis.OsmNodes;
import io.gazetteer.postgis.util.GeometryUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class Main {

  public static void main(String[] args) throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/osm?user=osm&password=osm");
    GeometryFactory gf = new GeometryFactory();
    byte[] b = GeometryUtil.writeGeometry(gf.createPoint(new Coordinate(1, 1)));

    OsmNodes.insert(connection, new OsmNodes.Row(3l, 1, 1, new Timestamp(2), 1l, new HashMap<>(), gf.createPoint(new Coordinate(1, 1))));
    OsmNodes.insert(connection, new OsmNodes.Row(4l, 1, 1, new Timestamp(2), 1l, new HashMap<>(), null));
    OsmNodes.Row row = OsmNodes.select(connection, new OsmNodes.PrimaryKey(4l));
    System.out.println(row);
  }

}
