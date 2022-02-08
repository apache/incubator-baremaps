package com.baremaps.store;

import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.LongDataType;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;

public class LongFixedSizeDataSparseMap<T> implements LongDataMap<T> {

  // The key space is broken into chunks of 256 and for each chunk, store:
  // 1) the index in the outputs array for the first key in the block
  private final FixedSizeDataList<Long> offsets;
  // 2) the number of leading 0's at the start of each block
  private final ByteArrayList offsetStartPad;

  private final FixedSizeDataList<T> values;
  private int lastChunk = -1;
  private int lastOffset = 0;

  public LongFixedSizeDataSparseMap(FixedSizeDataList<T> values) {
    this.offsets = new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory());
    this.offsetStartPad = new ByteArrayList();
    this.values = values;
  }

  @Override
  public void put(long key, T value) {
    long index = values.size();
    int chunk = (int) (key >>> 8);
    int offset = (int) (key & 255);

    if (chunk != lastChunk) {
      // new chunk, store offset and leading zeros
      lastOffset = offset;
      while (offsets.size() <= chunk) {
        offsets.add(index);
        offsetStartPad.add((byte) offset);
      }
      lastChunk = chunk;
    } else {
      // same chunk, write not_founds until we get to right idx
      while (++lastOffset < offset) {
        values.add(null);
      }
    }
    values.add(value);
  }

  @Override
  public T get(long key) {
    int chunk = (int) (key >>> 8);
    int offset = (int) (key & 255);
    if (chunk >= offsets.size()) {
      return null;
    }

    long lo = offsets.get(chunk);
    long hi = Math.min(values.size(), chunk >= offsets.size() - 1 ? values.size() : offsets.get(chunk + 1)) - 1;
    int startPad = offsetStartPad.get(chunk) & 255;

    long index = lo + offset - startPad;

    if (index > hi || index < lo) {
      return null;
    }

    return values.get(index);
  }

}
