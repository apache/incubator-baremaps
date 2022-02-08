package com.baremaps.store;

import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.LongDataType;

public class LongFixedSizeDataSortedMap<T> implements LongDataMap<T> {

  private final FixedSizeDataList<Long> offsets;
  private final FixedSizeDataList<Long> keys;
  private final FixedSizeDataList<T> values;
  private long lastChunk = -1;

  public LongFixedSizeDataSortedMap(FixedSizeDataList<Long> keys, FixedSizeDataList<T> values) {
    this.offsets = new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory());
    this.keys = keys;
    this.values = values;
  }

  @Override
  public void put(long key, T value) {
    long index = keys.size();
    long chunk = key >>> 8;
    if (chunk != lastChunk) {
      while (offsets.size() <= chunk) {
        offsets.add(index);
      }
      lastChunk = chunk;
    }
    keys.add(key);
    values.add(value);
  }

  @Override
  public T get(long key) {
    long chunk = key >>> 8;
    if (chunk >= offsets.size()) {
      return null;
    }
    long lo = offsets.get(chunk);
    long hi = Math.min(
        keys.size(),
        chunk >= offsets.size() - 1
            ? keys.size()
            : offsets.get(chunk + 1)) - 1;
    while (lo <= hi) {
      long index = (lo + hi) >>> 1;
      long value = keys.get(index);
      if (value < key) {
        lo = index + 1;
      } else if (value > key) {
        hi = index - 1;
      } else {
        // found
        return values.get(index);
      }
    }
    return null;
  }
}
