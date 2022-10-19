/*
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

package org.apache.baremaps.collection;



import org.apache.baremaps.collection.memory.OffHeapMemory;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.PairDataType.Pair;

/**
 * A map of data backed by a {@link AlignedDataList} for storing keys and a {@link DataStore} for
 * storing values.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class LongDataSortedMap<T> implements LongDataMap<T> {

  private final AlignedDataList<Long> offsets;
  private final AlignedDataList<Pair<Long, Long>> keys;
  private final DataStore<T> values;

  private long lastChunk = -1;

  public LongDataSortedMap(AlignedDataList<Pair<Long, Long>> keys, DataStore<T> values) {
    this.offsets = new AlignedDataList<>(new LongDataType(), new OffHeapMemory());
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
    long position = values.add(value);
    keys.add(new Pair<>(key, position));
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
      Pair<Long, Long> pair = keys.get(index);
      long value = pair.left();
      if (value < key) {
        lo = index + 1;
      } else if (value > key) {
        hi = index - 1;
      } else {
        // found
        return values.get(pair.right());
      }
    }
    return null;
  }
}
