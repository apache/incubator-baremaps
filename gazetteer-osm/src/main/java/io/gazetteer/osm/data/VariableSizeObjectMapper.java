package io.gazetteer.osm.data;

import java.nio.ByteBuffer;

public interface VariableSizeObjectMapper<T> {

  int size(T value);

  T read(ByteBuffer buffer);

  void write(ByteBuffer buffer, T value);

}
