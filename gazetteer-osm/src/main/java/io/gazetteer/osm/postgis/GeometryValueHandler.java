package io.gazetteer.osm.postgis;

import de.bytefish.pgbulkinsert.pgsql.handlers.BaseValueHandler;
import org.locationtech.jts.geom.Geometry;

import java.io.DataOutputStream;

public class GeometryValueHandler extends BaseValueHandler<Geometry> {

  @Override
  protected void internalHandle(DataOutputStream buffer, Geometry value) throws Exception {
    byte[] wkb = GeometryUtil.asWKB(value);
    buffer.writeInt(wkb.length);
    buffer.write(wkb);
  }
}
