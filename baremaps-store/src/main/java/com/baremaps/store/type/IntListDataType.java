package com.baremaps.store.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IntListDataType implements DataType<List<Integer>> {

  @Override
  public int size(List<Integer> values) {
    return 4 + values.size() * 4;
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<Integer> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Integer value : values) {
      buffer.putInt(position, value);
      position += 4;
    }
  }

  @Override
  public List<Integer> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Integer> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.getInt(position));
      position += 4;
    }
    return list;
  }
}
