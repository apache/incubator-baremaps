package io.gazetteer.osm.data;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DirectByteBufferProvider implements ByteBufferProvider {

  private final int count;
  private final int size;

  private final Map<Integer, ByteBuffer> buffers;

  public DirectByteBufferProvider() {
    this(1024 * 1024, 64 * 1024);
  }

  public DirectByteBufferProvider(int count, int size) {
    this.count = count;
    this.size = size;
    this.buffers = new ConcurrentHashMap<>(this.count);
  }

  public int count() {
    return count;
  }

  public int size() {
    return size;
  }

  public ByteBuffer get(int index) {
    return buffers.computeIfAbsent(index, i -> ByteBuffer.allocateDirect(size));
  }

}
