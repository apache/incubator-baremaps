package com.baremaps.store.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ListDataType<T> implements DataType<List<T>> {

  public final DataType<T> dataType;

  public ListDataType(DataType<T> dataType) {
    this.dataType = dataType;
  }

  @Override
  public int size(List<T> values) {
    int size = 4;
    for (T value : values) {
      size += dataType.size(value);
    }
    return size;
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<T> values) {
    buffer.putInt(position, values.size());
    for (T value : values) {
      position += dataType.size(value);
      dataType.write(buffer, position, value);
    }
  }

  @Override
  public List<T> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<T> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      T value = dataType.read(buffer, position);
      position += dataType.size(value);
      list.add(value);
    }
    return list;
  }
}
