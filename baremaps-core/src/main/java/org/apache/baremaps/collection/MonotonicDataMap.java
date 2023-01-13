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



import java.util.Iterator;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.PairDataType;
import org.apache.baremaps.collection.type.PairDataType.Pair;

/**
 * A map of data backed by a {@link DataList} for storing keys and a {@link AppendOnlyBuffer} for
 * storing values.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class MonotonicDataMap<T> extends DataMap<T> {

  private final DataList<Long> offsets;
  private final DataList<Pair<Long, Long>> keys;
  private final AppendOnlyBuffer<T> values;

  private long lastChunk = -1;

  public MonotonicDataMap(AppendOnlyBuffer<T> values) {
    this(
        values,
        new MemoryAlignedDataList<>(new PairDataType<>(new LongDataType(), new LongDataType())),
        new MemoryAlignedDataList<>(new LongDataType()));
  }

  public MonotonicDataMap(AppendOnlyBuffer<T> values, DataList<Pair<Long, Long>> keys) {
    this(
        values,
        keys,
        new MemoryAlignedDataList<>(new LongDataType()));
  }


  public MonotonicDataMap(AppendOnlyBuffer<T> values, DataList<Pair<Long, Long>> keys,
      DataList<Long> offsets) {
    this.offsets = offsets;
    this.keys = keys;
    this.values = values;
  }

  public T put(Long key, T value) {
    long index = keys.sizeAsLong();
    long chunk = key >>> 8;
    if (chunk != lastChunk) {
      while (offsets.sizeAsLong() <= chunk) {
        offsets.append(index);
      }
      lastChunk = chunk;
    }
    long position = values.append(value);
    keys.append(new Pair<>(key, position));
    return value;
  }

  public T get(Object keyObject) {
    long key = (long) keyObject;
    long chunk = key >>> 8;
    if (chunk >= offsets.sizeAsLong()) {
      return null;
    }
    long lo = offsets.get(chunk);
    long hi =
        Math.min(
            keys.sizeAsLong(),
            chunk >= offsets.sizeAsLong() - 1
                ? keys.sizeAsLong()
                : offsets.get(chunk + 1))
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

  @Override
  protected Iterator<Long> keyIterator() {
    return keys.stream().map(Pair::left).iterator();
  }

  @Override
  protected Iterator<T> valueIterator() {
    return keys.stream().map(Pair::right).map(values::get).iterator();
  }

  @Override
  protected Iterator<Entry<Long, T>> entryIterator() {
    return keys.stream().map(Pair::right).map(this::entry).iterator();
  }

  private Entry<Long, T> entry(long key) {
    return new Entry() {
      @Override
      public Long getKey() {
        return key;
      }

      @Override
      public T getValue() {
        return get(key);
      }

      @Override
      public T setValue(Object value) {
        return put(key, (T) value);
      }
    };
  }

  @Override
  public long sizeAsLong() {
    return keys.sizeAsLong();
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    return values.contains(value);
  }

  @Override
  public T remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    offsets.clear();
    keys.clear();
    values.clear();
  }
}
