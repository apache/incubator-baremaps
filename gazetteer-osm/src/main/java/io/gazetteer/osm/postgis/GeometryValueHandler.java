package io.gazetteer.osm.postgis;

import de.bytefish.pgbulkinsert.pgsql.handlers.BaseValueHandler;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

import java.io.DataOutputStream;
import java.nio.ByteOrder;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

public class GeometryValueHandler extends BaseValueHandler<Geometry> {

  @Override
  protected void internalHandle(DataOutputStream buffer, Geometry value) throws Exception {
    WKBWriter writer = new WKBWriter(2, wkbNDR);
    byte[] wkb = writer.write(value);
    buffer.writeInt(wkb.length);
    buffer.write(wkb);
  }
}
