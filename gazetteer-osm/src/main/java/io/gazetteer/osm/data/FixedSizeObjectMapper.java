package io.gazetteer.osm.data;

import java.nio.ByteBuffer;

public interface FixedSizeObjectMapper<T> {

  int size();

  T read(ByteBuffer buffer);

  void write(ByteBuffer buffer, T value);

}
