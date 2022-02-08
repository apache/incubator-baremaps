package com.baremaps.store.type;

import java.nio.ByteBuffer;
import org.locationtech.jts.geom.Coordinate;

public class CoordinateDataType implements FixedSizeDataType<Coordinate> {

  @Override
  public int size(Coordinate value) {
    return 16;
  }

  @Override
  public void write(ByteBuffer buffer, int position, Coordinate value) {
    buffer.putDouble(position, value.x);
    buffer.putDouble(position + 8, value.y);
  }

  @Override
  public Coordinate read(ByteBuffer buffer, int position) {
    double x = buffer.getDouble(position);
    double y = buffer.getDouble(position + 8);
    return new Coordinate(x, y);
  }
}
