package com.baremaps.postgres.handlers;

import static org.locationtech.jts.io.WKBConstants.wkbNDR;

import java.io.DataOutputStream;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

public class PostgisGeometryValueHandler implements ValueHandler<Geometry> {

  @Override
  public void handle(DataOutputStream buffer, @Nullable Geometry value) throws IOException {
    WKBWriter writer = new WKBWriter(2, wkbNDR, true);
    byte[] wkb = writer.write(value);
    buffer.writeInt(wkb.length);
    buffer.write(wkb, 0, wkb.length);
  }
}
