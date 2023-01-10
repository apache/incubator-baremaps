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



import org.apache.baremaps.collection.store.DataStore;
import org.apache.baremaps.collection.store.MemoryAlignedDataStore;
import org.apache.baremaps.collection.type.LongDataType;

/**
 * A sorted map of data backed by {@link DataStore}s for storing keys and values.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class SortedLongFixedSizeDataMap<T> implements LongMap<T> {

  private final DataStore<Long> offsets;
  private final DataStore<Long> keys;
  private final DataStore<T> values;
  private long lastChunk = -1;

  public SortedLongFixedSizeDataMap(DataStore<T> values) {
    this(new MemoryAlignedDataStore<>(new LongDataType()),
        new MemoryAlignedDataStore<>(new LongDataType()), values);
  }

  /**
   * Constructs a map.
   *
   * @param keys the list of keys
   * @param values the list of values
   */
  public SortedLongFixedSizeDataMap(DataStore<Long> offsets, DataStore<Long> keys,
      DataStore<T> values) {
    this.offsets = offsets;
    this.keys = keys;
    this.values = values;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public T get(long key) {
    long chunk = key >>> 8;
    if (chunk >= offsets.size()) {
      return null;
    }
    long lo = offsets.get(chunk);
    long hi = Math.min(
        keys.size(),
        chunk >= offsets.size() - 1 ? keys.size() : offsets.get(chunk + 1))
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
        return values.get(index);
      }
    }
    return null;
  }

}
