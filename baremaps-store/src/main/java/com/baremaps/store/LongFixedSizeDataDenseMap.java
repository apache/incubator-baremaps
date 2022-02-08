package com.baremaps.store;

import com.baremaps.store.memory.Memory;
import com.baremaps.store.type.FixedSizeDataType;
import java.nio.ByteBuffer;

public class LongFixedSizeDataDenseMap<T> implements LongDataMap<T> {

  private final FixedSizeDataType<T> dataType;

  private final int valueShift;

  private final Memory memory;

  private final long segmentBits;

  private final long segmentMask;

  public LongFixedSizeDataDenseMap(FixedSizeDataType<T> dataType, Memory memory) {
    if (dataType.size() > memory.segmentBytes()) {
      throw new RuntimeException("The values are too big");
    }
    this.dataType = dataType;
    this.memory = memory;
    this.valueShift = (int) (Math.log(dataType.size()) + 1);
    this.segmentBits = memory.segmentBits();
    this.segmentMask = memory.segmentMask();
  }

  @Override
  public void put(long key, T value) {
    long position = key << valueShift;
    int segmentIndex = (int) (position >>> segmentBits);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    dataType.write(segment, segmentOffset, value);
  }

  @Override
  public T get(long key) {
    long position = key << valueShift;
    int segmentIndex = (int) (position >> segmentBits);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }
}
