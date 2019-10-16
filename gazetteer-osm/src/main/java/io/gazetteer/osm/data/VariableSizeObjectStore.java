package io.gazetteer.osm.data;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

public class VariableSizeObjectStore<T> {

  private final AtomicLong position = new AtomicLong(0);

  private final DirectByteBufferProvider buffers;

  private final VariableSizeObjectMapper<T> variableSizeObjectMapper;

  public VariableSizeObjectStore(DirectByteBufferProvider buffers, VariableSizeObjectMapper<T> variableSizeObjectMapper) {
    this.buffers = buffers;
    this.variableSizeObjectMapper = variableSizeObjectMapper;
  }

  private int index(long position) {
    return Math.toIntExact(position / buffers.size());
  }

  private int offset(long position) {
    return Math.toIntExact(position % buffers.size());
  }

  public T read(long position) {
    int index = index(position);
    int offset = offset(position);

    ByteBuffer buffer = buffers.get(index);
    buffer.position(offset);

    return variableSizeObjectMapper.read(buffer);
  }

  public long write(T value) {
    int size = variableSizeObjectMapper.size(value);
    long position = this.position.getAndAdd(buffers.size());

    int index = index(position);
    int offset = offset(position);

    if (offset + size > buffers.size()) {
      index += 1;
      offset = 0;
      position = buffers.size() * index;
    }

    ByteBuffer buffer = buffers.get(index);
    buffer.position(offset);

    variableSizeObjectMapper.write(buffer, value);

    return position;
  }



}
