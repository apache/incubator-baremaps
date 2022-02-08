package com.baremaps.store.type;

import java.nio.ByteBuffer;

public class FloatDataType implements FixedSizeDataType<Float> {

  @Override
  public int size(Float value) {
    return 4;
  }

  @Override
  public void write(ByteBuffer buffer, int position, Float value) {
    buffer.putFloat(position, value);
  }

  @Override
  public Float read(ByteBuffer buffer, int position) {
    return buffer.getFloat(position);
  }
}
