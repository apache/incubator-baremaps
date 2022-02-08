package com.baremaps.store.type;

import java.nio.ByteBuffer;

public class DoubleDataType implements FixedSizeDataType<Double> {

  @Override
  public int size(Double value) {
    return 8;
  }

  @Override
  public void write(ByteBuffer buffer, int position, Double value) {
    buffer.putDouble(position, value);
  }

  @Override
  public Double read(ByteBuffer buffer, int position) {
    return buffer.getDouble(position);
  }
}
