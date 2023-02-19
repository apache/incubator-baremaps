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
import java.util.Map;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.PairDataType;
import org.apache.baremaps.collection.type.PairDataType.Pair;

/**
 * A map that can hold a large number of variable-size data elements. The elements must be sorted by
 * their key and inserted in a monotonic way. The elements cannot be removed or updated once
 * inserted.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class MonotonicDataMap<E> extends DataMap<E> {

  private final DataList<Long> offsets;
  private final DataList<Pair<Long, Long>> keys;
  private final AppendOnlyBuffer<E> values;

  private long lastChunk = -1;

  /**
   * Constructs a map with default lists for storing offsets.
   *
   * @param values the buffer of values
   */
  public MonotonicDataMap(AppendOnlyBuffer<E> values) {
    this(
        new MemoryAlignedDataList<>(new LongDataType()),
        new MemoryAlignedDataList<>(new PairDataType<>(new LongDataType(), new LongDataType())),
        values);
  }

  /**
   * Constructs a map with default lists for storing offsets and keys.
   *
   * @param keys the list of keys
   * @param values the buffer of values
   */
  public MonotonicDataMap(DataList<Pair<Long, Long>> keys, AppendOnlyBuffer<E> values) {
    this(
        new MemoryAlignedDataList<>(new LongDataType()),
        keys,
        values);
  }

  /**
   * Constructs a map.
   *
   * @param offsets the list of offsets
   * @param keys the list of keys
   * @param values the buffer of values
   */
  public MonotonicDataMap(DataList<Long> offsets, DataList<Pair<Long, Long>> keys,
      AppendOnlyBuffer<E> values) {
    this.offsets = offsets;
    this.keys = keys;
    this.values = values;
  }

  /** {@inheritDoc} */
  public E put(Long key, E value) {
    long index = keys.sizeAsLong();
    long chunk = key >>> 8;
    if (chunk != lastChunk) {
      while (offsets.sizeAsLong() <= chunk) {
        offsets.add(index);
      }
      lastChunk = chunk;
    }
    long position = values.addPositioned(value);
    keys.add(new Pair<>(key, position));
    return null;
  }

  /** {@inheritDoc} */
  public E get(Object keyObject) {
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
        return values.read(pair.right());
      }
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  protected Iterator<Long> keyIterator() {
    return keys.stream().map(Pair::left).iterator();
  }

  /** {@inheritDoc} */
  @Override
  protected Iterator<E> valueIterator() {
    return keys.stream().map(Pair::right).map(values::read).iterator();
  }

  /** {@inheritDoc} */
  @Override
  protected Iterator<Entry<Long, E>> entryIterator() {
    return keys.stream()
        .map(p -> Map.entry(p.left(), values.read(p.right())))
        .iterator();
  }

  /** {@inheritDoc} */
  @Override
  public long sizeAsLong() {
    return keys.sizeAsLong();
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsValue(Object value) {
    return values.contains(value);
  }

  /** {@inheritDoc} */
  @Override
  public E remove(Object key) {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    offsets.clear();
    keys.clear();
    values.clear();
  }
}
