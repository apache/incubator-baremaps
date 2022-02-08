package com.baremaps.store.type;

import java.nio.ByteBuffer;

public class ByteDataType implements FixedSizeDataType<Byte> {

  @Override
  public int size(Byte value) {
    return 1;
  }

  @Override
  public void write(ByteBuffer buffer, int position, Byte value) {
    buffer.put(position, value);
  }

  @Override
  public Byte read(ByteBuffer buffer, int position) {
    return buffer.get(position);
  }
}
