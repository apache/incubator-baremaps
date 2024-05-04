/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.database.collection;



import com.google.common.collect.Streams;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.baremaps.database.type.LongDataType;

/**
 * A map that can hold a large number of fixed-size data elements. The elements must be sorted by
 * their key and inserted in a monotonic way. The elements cannot be removed or updated once
 * inserted.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class MonotonicFixedSizeDataMap<E> implements DataMap<Long, E> {

  private final DataList<Long> offsets;
  private final DataList<Long> keys;
  private final DataList<E> values;
  private long lastChunk = -1;

  /**
   * Constructs a map with default lists for storing offsets and keys.
   *
   * @param values the list of values
   */
  public MonotonicFixedSizeDataMap(DataList<E> values) {
    this(new MemoryAlignedDataList<>(new LongDataType()),
        new MemoryAlignedDataList<>(new LongDataType()), values);
  }

  /**
   * Constructs a map.
   *
   * @param offsets the list of offsets
   * @param keys the list of keys
   * @param values the list of values
   */
  public MonotonicFixedSizeDataMap(
      DataList<Long> offsets,
      DataList<Long> keys,
      DataList<E> values) {
    this.offsets = offsets;
    this.keys = keys;
    this.values = values;
  }

  /** {@inheritDoc} */
  public E get(Object keyObject) {
    long key = (long) keyObject;
    long chunk = key >>> 8;
    if (chunk >= offsets.size()) {
      return null;
    }
    long lo = offsets.get(chunk);
    long hi = Math.min(keys.size(),
        chunk >= offsets.size() - 1 ? keys.size() : offsets.get(chunk + 1)) - 1;
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

  /** {@inheritDoc} */
  public E put(Long key, E value) {
    long size = keys.size();
    long chunk = key >>> 8;
    if (chunk != lastChunk) {
      while (offsets.size() <= chunk) {
        offsets.add(size);
      }
      lastChunk = chunk;
    }
    keys.add(key);
    values.add(value);
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public E remove(Long key) {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public long size() {
    return keys.size();
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(Object key) {
    return keys.contains(key);
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsValue(Object value) {
    return values.contains(value);
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Long> keyIterator() {
    return keys.iterator();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<E> valueIterator() {
    return values.iterator();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Entry<Long, E>> entryIterator() {
    return Streams.zip(
        Streams.stream(keyIterator()),
        Streams.stream(valueIterator()),
        Map::entry).iterator();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    offsets.clear();
    keys.clear();
    values.clear();
  }

}
