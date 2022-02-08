package com.baremaps.store.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FloatListDataType implements DataType<List<Float>> {

  @Override
  public int size(List<Float> values) {
    return 4 + values.size() * 4;
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<Float> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Float value : values) {
      buffer.putFloat(position, value);
      position += 4;
    }
  }

  @Override
  public List<Float> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Float> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.getFloat(position));
      position += 4;
    }
    return list;
  }
}
