package com.baremaps.osm.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;

public class LmdbReferenceCache extends LmdbCache<Long, List<Long>> {

  public LmdbReferenceCache(Env<ByteBuffer> env, Dbi<ByteBuffer> database) {
    super(env, database);
  }

  @Override
  public ByteBuffer buffer(Long key) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(20);
    buffer.put(String.format("%020d", key).getBytes()).flip();
    return buffer.putLong(key);
  }

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

  public ByteBuffer write(List<Long> value) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(4 + 8 * value.size());
    buffer.putInt(value.size());
    for (Long v : value) {
      buffer.putLong(v);
    }
    buffer.flip();
    return buffer;
  }

}
