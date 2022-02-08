package com.baremaps.store;

import com.baremaps.store.memory.Memory;
import com.baremaps.store.type.DataType;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataStore<T> {

  private final DataType<T> dataType;
  private final Memory memory;
  private final int segmentBytes;
  private long offset;
  private long size;

  private Lock lock = new ReentrantLock();

  public DataStore(DataType<T> dataType, Memory memory) {
    this.dataType = dataType;
    this.memory = memory;
    this.segmentBytes = memory.segmentBytes();
    this.offset = 0;
    this.size = 0;
  }

  public long add(T value) {
    int size = dataType.size(value);
    if (size > segmentBytes) {
      throw new RuntimeException("The value is too big to fit in a segment");
    }

    lock.lock();
    long position = offset;
    int segmentIndex = (int) (position / segmentBytes);
    int segmentOffset = (int) (position % segmentBytes);
    if (segmentOffset + size > segmentBytes) {
      segmentOffset = 0;
      segmentIndex = segmentIndex + 1;
      position = segmentIndex * segmentBytes;
    }
    offset = position + size;
    this.size++;
    lock.unlock();

    ByteBuffer segment = memory.segment(segmentIndex);
    dataType.write(segment, segmentOffset, value);

    return position;
  }

  public T get(long position) {
    int segmentIndex = (int) position / segmentBytes;
    int segmentOffset = (int) position % segmentBytes;
    ByteBuffer buffer = memory.segment(segmentIndex);
    return dataType.read(buffer, segmentOffset);
  }

  public long bytes() {
    return offset;
  }

  public long size() {
    return size;
  }
}
