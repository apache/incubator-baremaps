package com.baremaps.store.type;

import java.nio.ByteBuffer;

public class LongDataType implements FixedSizeDataType<Long> {

  @Override
  public int size(Long value) {
    return 8;
  }

  @Override
  public void write(ByteBuffer buffer, int position, Long value) {
    buffer.putLong(position, value);
  }

  @Override
  public Long read(ByteBuffer buffer, int position) {
    return buffer.getLong(position);
  }
}
