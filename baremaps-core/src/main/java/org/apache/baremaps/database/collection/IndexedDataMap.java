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
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link DataMap} that can hold a large number of variable size data elements. This data map is
 * backed by an index and a buffer that can be either heap, off-heap, or memory mapped.
 *
 * @param <E> The type of the elements.
 */
public class IndexedDataMap<E> implements DataMap<Long, E> {

  private final Map<Long, Long> index;

  private final AppendOnlyLog<E> values;

  /**
   * Constructs a {@link IndexedDataMap}.
   *
   * @param values the values
   */
  public IndexedDataMap(AppendOnlyLog<E> values) {
    this(new Long2LongOpenHashMap(), values);
  }

  /**
   * Constructs a {@link IndexedDataMap}.
   *
   * @param index the index
   * @param values the values
   */
  public IndexedDataMap(Map<Long, Long> index, AppendOnlyLog<E> values) {
    this.index = index;
    this.values = values;
  }

  /** {@inheritDoc} */
  @Override
  public E put(Long key, E value) {
    var oldIndex = index.get(key);
    var position = values.addPositioned(value);
    index.put(key, position);
    return oldIndex == null ? null : values.getPositioned(oldIndex);
  }

  /** {@inheritDoc} */
  @Override
  public E get(Object key) {
    var position = index.get(key);
    return position == null ? null : values.getPositioned(position);
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Long> keyIterator() {
    return index.keySet().iterator();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<E> valueIterator() {
    return Streams.stream(keyIterator()).map(this::get).iterator();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Entry<Long, E>> entryIterator() {
    return Streams.stream(keyIterator()).map(k -> Map.entry(k, get(k))).iterator();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return index.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public long size() {
    return index.size();
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(Object key) {
    return index.containsKey(key);
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsValue(Object value) {
    return index.values().stream().map(values::getPositioned).anyMatch(value::equals);
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    index.clear();
    values.clear();
  }
}
