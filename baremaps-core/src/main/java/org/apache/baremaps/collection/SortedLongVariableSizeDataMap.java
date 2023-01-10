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



import org.apache.baremaps.collection.store.AppendOnlyStore;
import org.apache.baremaps.collection.store.DataStore;
import org.apache.baremaps.collection.store.MemoryAlignedDataStore;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.PairDataType;
import org.apache.baremaps.collection.type.PairDataType.Pair;

/**
 * A map of data backed by a {@link DataStore} for storing keys and a {@link AppendOnlyStore} for
 * storing values.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class SortedLongVariableSizeDataMap<T> implements LongMap<T> {

  private final DataStore<Long> offsets;
  private final DataStore<Pair<Long, Long>> keys;
  private final AppendOnlyStore<T> values;

  private long lastChunk = -1;

  public SortedLongVariableSizeDataMap(AppendOnlyStore<T> values) {
    this(
        new MemoryAlignedDataStore<>(new LongDataType()),
        new MemoryAlignedDataStore<>(new PairDataType<>(new LongDataType(), new LongDataType())),
        values);
  }

  public SortedLongVariableSizeDataMap(DataStore<Pair<Long, Long>> keys,
      AppendOnlyStore<T> values) {
    this(
        new MemoryAlignedDataStore<>(new LongDataType()),
        keys,
        values);
  }


  public SortedLongVariableSizeDataMap(DataStore<Long> offsets, DataStore<Pair<Long, Long>> keys,
      AppendOnlyStore<T> values) {
    this.offsets = offsets;
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
