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



import com.google.common.collect.Streams;
import java.util.Iterator;
import org.apache.baremaps.collection.type.LongDataType;

/**
 * A sorted map of data backed by {@link DataList}s for storing keys and values.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class MonotonicFixedSizeDataMap<T> extends DataMap<T> {

  private final DataList<Long> offsets;
  private final DataList<Long> keys;
  private final DataList<T> values;
  private long lastChunk = -1;

  public MonotonicFixedSizeDataMap(DataList<T> values) {
    this(new MemoryAlignedDataList<>(new LongDataType()),
        new MemoryAlignedDataList<>(new LongDataType()), values);
  }

  /**
   * Constructs a map.
   *
   * @param keys the list of keys
   * @param values the list of values
   */
  public MonotonicFixedSizeDataMap(
      DataList<Long> offsets,
      DataList<Long> keys,
      DataList<T> values) {
    this.offsets = offsets;
    this.keys = keys;
    this.values = values;
  }


  /**
   * {@inheritDoc}
   */
  public T get(Object keyObject) {
    long key = (long) keyObject;
    long chunk = key >>> 8;
    if (chunk >= offsets.size()) {
      return null;
    }
    long lo = offsets.get(chunk);
    long hi = Math.min(keys.sizeAsLong(),
        chunk >= offsets.sizeAsLong() - 1 ? keys.sizeAsLong() : offsets.get(chunk + 1)) - 1;
    while (lo <= hi) {
      long index = (lo + hi) >>> 1;
      long value = keys.get(index);
      if (value < key) {
        lo = index + 1;
      } else if (value > key) {
        hi = index - 1;
      } else {
        return values.get(index);
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public T put(Long key, T value) {
    long size = keys.sizeAsLong();
    long chunk = key >>> 8;
    if (chunk != lastChunk) {
      while (offsets.sizeAsLong() <= chunk) {
        offsets.add(size);
      }
      lastChunk = chunk;
    }
    keys.add(key);
    values.add(value);
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T remove(Object key) {
    throw new UnsupportedOperationException();
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return keys.sizeAsLong();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsKey(Object key) {
    return keys.contains(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsValue(Object value) {
    return values.contains(value);
  }

  @Override
  protected Iterator<Long> keyIterator() {
    return keys.iterator();
  }

  @Override
  protected Iterator<T> valueIterator() {
    return values.iterator();
  }

  @Override
  protected Iterator<Entry<Long, T>> entryIterator() {
    return Streams.zip(
        Streams.stream(keyIterator()),
        Streams.stream(valueIterator()),
        this::newEntry).iterator();
  }

  private Entry<Long, T> newEntry(Long key, T value) {
    return new Entry<Long, T>() {
      @Override
      public Long getKey() {
        return key;
      }

      @Override
      public T getValue() {
        return value;
      }

      @Override
      public T setValue(T value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    offsets.clear();
    keys.clear();
    values.clear();
  }
}
