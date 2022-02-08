package com.baremaps.store.type;

import java.nio.ByteBuffer;

public class ShortDataType implements FixedSizeDataType<Short> {

  @Override
  public int size(Short value) {
    return 2;
  }

  @Override
  public void write(ByteBuffer buffer, int position, Short value) {
    buffer.putShort(position, value);
  }

  @Override
  public Short read(ByteBuffer buffer, int position) {
    return buffer.getShort(position);
  }

}
