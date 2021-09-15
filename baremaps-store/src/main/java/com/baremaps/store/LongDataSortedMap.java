/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.store;

import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.LongDataType;

public class LongDataSortedMap<T> implements LongDataMap<T> {

  private final FixedSizeDataList<Long> offsets;
  private final FixedSizeDataList<Long> keys;
  private final FixedSizeDataList<Long> positions;
  private final DataStore<T> values;
  private long lastChunk = -1;

  public LongDataSortedMap(DataStore<T> values) {
    this.offsets = new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory());
    this.keys = new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory());
    this.positions = new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory());
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
    long position = values.add(value);
    positions.add(position);
  }

  @Override
  public T get(long key) {
    long chunk = key >>> 8;
    if (chunk >= offsets.size()) {
      return null;
    }
    long lo = offsets.get(chunk);
    long hi =
        Math.min(keys.size(), chunk >= offsets.size() - 1 ? keys.size() : offsets.get(chunk + 1))
            - 1;
    while (lo <= hi) {
      long index = (lo + hi) >>> 1;
      long value = keys.get(index);
      if (value < key) {
        lo = index + 1;
      } else if (value > key) {
        hi = index - 1;
      } else {
        // found
        long position = positions.get(index);
        return values.get(position);
      }
    }
    return null;
  }
}