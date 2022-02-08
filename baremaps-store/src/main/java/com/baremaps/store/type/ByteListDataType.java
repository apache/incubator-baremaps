package com.baremaps.store.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteListDataType implements DataType<List<Byte>> {

  @Override
  public int size(List<Byte> values) {
    return 4 + values.size();
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<Byte> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Byte value : values) {
      buffer.put(position, value);
      position++;
    }
  }

  @Override
  public List<Byte> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Byte> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.get(position));
      position++;
    }
    return list;
  }
}
