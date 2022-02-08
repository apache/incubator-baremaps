package com.baremaps.store.type;

import java.nio.ByteBuffer;

public interface DataType<T> {

  int size(T value);

  void write(ByteBuffer buffer, int position, T value);

  T read(ByteBuffer buffer, int position);

}
