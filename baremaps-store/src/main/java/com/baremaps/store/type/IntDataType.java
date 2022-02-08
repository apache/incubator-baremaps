package com.baremaps.store.type;

import java.nio.ByteBuffer;

public class IntDataType implements FixedSizeDataType<Integer> {

  @Override
  public int size(Integer value) {
    return 4;
  }

  @Override
  public void write(ByteBuffer buffer, int position, Integer value) {
    buffer.putInt(position, value);
  }

  @Override
  public Integer read(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }
}
