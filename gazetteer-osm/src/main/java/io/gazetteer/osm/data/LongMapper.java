package io.gazetteer.osm.data;

import java.nio.ByteBuffer;

public class LongMapper implements FixedSizeObjectMapper<Long> {

  @Override
  public int size() {
    return 9;
  }

  @Override
  public Long read(ByteBuffer buffer) {
    if (buffer.get() == 0) {
      return null;
    }
    return buffer.getLong();
  }

  @Override
  public void write(ByteBuffer buffer, Long value) {
    if (value != null) {
      buffer.put((byte) 1);
      buffer.putLong(value);
    }
  }
}
