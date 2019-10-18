package io.gazetteer.osm.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ReferenceMapper implements CacheMapper<List<Long>> {

  @Override
  public List<Long> read(ByteBuffer buffer) {
    if (buffer == null) {
      return null;
    }
    int size = buffer.getInt();
    List<Long> values = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      values.add(buffer.getLong());
    }
    return values;
  }

  @Override
  public ByteBuffer write(List<Long> values) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(4 + 8 * values.size());
    buffer.putInt(values.size());
    for (Long value : values) {
      buffer.putLong(value);
    }
    buffer.flip();
    return buffer;
  }

}
