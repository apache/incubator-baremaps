package io.gazetteer.tilestore.util;

import io.gazetteer.tilestore.XYZ;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

public class TileUtil {

  public static final String BBOX = "SELECT st_asewkb(st_transform(st_setsrid(st_extent(geom), 3857), 4326)) as table_extent FROM osm_nodes;";

  public static Geometry bbox(Connection connection) throws SQLException, ParseException {
    try (PreparedStatement statement = connection.prepareStatement(BBOX)) {
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return new WKBReader().read(result.getBytes(1));
      } else {
        return null;
      }
    }
  }

  public static List<XYZ> overlappingXYZ(Geometry geometry, int minZ, int maxZ) {
    ArrayList<XYZ> coordinates = new ArrayList<>();
    Envelope envelope = geometry.getEnvelopeInternal();
    for (int z = minZ; z <= maxZ; z++) {
      XYZ min = xyz(envelope.getMinX(), envelope.getMaxY(), z);
      XYZ max = xyz(envelope.getMaxX(), envelope.getMinY(), z);
      for (int x = min.getX(); x <= max.getX(); x++) {
        for (int y = min.getY(); y <= max.getY(); y++) {
          XYZ xyz = new XYZ(x, y, z);
          coordinates.add(xyz);
        }
      }
    }
    return coordinates;
  }

  public static XYZ xyz(double lon, double lat, int z) {
    int x = (int) ((lon + 180.0) / 360.0 * (1 << z));
    int y = (int) ((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * (1 << z));
    return new XYZ(x, y, z);
  }

}
