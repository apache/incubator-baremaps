package com.baremaps.store.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LongListDataType implements DataType<List<Long>> {

  @Override
  public int size(List<Long> values) {
    return 4 + values.size() * 8;
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<Long> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Long value : values) {
      buffer.putLong(position, value);
      position += 8;
    }
  }

  @Override
  public List<Long> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Long> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.getLong(position));
      position += 8;
    }
    return list;
  }
}
