package com.baremaps.store.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DoubleListDataType implements DataType<List<Double>> {

  @Override
  public int size(List<Double> values) {
    return 4 + values.size() * 8;
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<Double> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Double value : values) {
      buffer.putDouble(position, value);
      position += 8;
    }
  }

  @Override
  public List<Double> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Double> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.getDouble(position));
      position += 8;
    }
    return list;
  }
}
