package io.gazetteer.common.postgis.util;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import java.sql.SQLException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

public class GeometryUtil {

  public static Geometry readGeometry(byte[] bytes) throws SQLException {
    if (bytes == null) {
      return null;
    } else {
      try {
        return new WKBReader().read(bytes);
      } catch (ParseException e) {
        throw new SQLException(e);
      }
    }
  }

  public static byte[] writeGeometry(Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      return new WKBWriter(2, wkbNDR, true).write(geometry);
    }
  }

}
