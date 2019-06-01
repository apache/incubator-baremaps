package io.gazetteer.postgis;

import java.sql.SQLException;
import java.util.Arrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.postgresql.util.PGBinaryObject;

public class PGGeometry implements PGBinaryObject {

  private Geometry geometry;

  public PGGeometry setGeometry(Geometry geometry) {
    this.geometry = geometry;
    return this;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  @Override
  public int lengthInBytes() {
    return new WKBWriter().write(geometry).length;
  }

  @Override
  public void setByteValue(byte[] value, int offset) throws SQLException {
    try {
      geometry = new WKBReader().read(Arrays.copyOfRange(value, offset, value.length));
    } catch (ParseException e) {
      throw new SQLException("Unable to parse the WKB string");
    }
  }

  @Override
  public void toBytes(byte[] bytes, int offset) {
    byte[] b = new WKBWriter().write(geometry);
    System.arraycopy(b, 0, bytes, offset, bytes.length);
  }

}
