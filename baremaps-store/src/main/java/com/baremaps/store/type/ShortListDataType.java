package com.baremaps.store.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ShortListDataType implements DataType<List<Short>> {

  @Override
  public int size(List<Short> values) {
    return 4 + values.size() * 2;
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<Short> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Short value : values) {
      buffer.putShort(position, value);
      position += 2;
    }
  }

  @Override
  public List<Short> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Short> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.getShort(position));
      position += 2;
    }
    return list;
  }
}
