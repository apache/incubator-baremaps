package io.gazetteer.osm.data;

import java.nio.ByteBuffer;

public class FixedSizeObjectMap<T> {

  private final ByteBufferProvider byteBufferProvider;
  private final FixedSizeObjectMapper<T> fixedSizeObjectMapper;
  private final int objectCount;

  public FixedSizeObjectMap(ByteBufferProvider byteBufferProvider, FixedSizeObjectMapper<T> fixedSizeObjectMapper) {
    this.byteBufferProvider = byteBufferProvider;
    this.fixedSizeObjectMapper = fixedSizeObjectMapper;
    this.objectCount = byteBufferProvider.size() / fixedSizeObjectMapper.size();
  }

  private int index(long position) {
    return Math.toIntExact(position / objectCount);
  }

  private int offset(long position) {
    return Math.toIntExact((position % objectCount) * fixedSizeObjectMapper.size());
  }

  public T get(long position) {
    int index = index(position);
    int offset = offset(position);
    ByteBuffer buffer = byteBufferProvider.get(index);
    buffer.position(offset);
    return fixedSizeObjectMapper.read(buffer);
  }

  public void set(long position, T value) {
    int index = index(position);
    int offset = offset(position);
    ByteBuffer buffer = byteBufferProvider.get(index);
    buffer.position(offset);
    fixedSizeObjectMapper.write(buffer, value);
  }

}
