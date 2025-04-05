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

package org.apache.baremaps.data.collection;



import com.google.common.collect.Streams;
import java.util.HashMap;
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
   * Static factory method to create a new builder.
   *
   * @param <E> the type of elements
   * @return a new builder
   */
  public static <E> Builder<E> builder() {
    return new Builder<>();
  }

  /**
   * Builder for {@link IndexedDataMap}.
   *
   * @param <E> the type of elements
   */
  public static class Builder<E> {
    private Map<Long, Long> index;
    private AppendOnlyLog<E> values;

    /**
     * Sets the index for the map.
     *
     * @param index the index
     * @return this builder
     */
    public Builder<E> index(Map<Long, Long> index) {
      this.index = index;
      return this;
    }

    /**
     * Sets the values for the map.
     *
     * @param values the values
     * @return this builder
     */
    public Builder<E> values(AppendOnlyLog<E> values) {
      this.values = values;
      return this;
    }

    /**
     * Builds a new {@link IndexedDataMap}.
     *
     * @return a new IndexedDataMap
     * @throws IllegalStateException if values are missing
     */
    public IndexedDataMap<E> build() {
      if (values == null) {
        throw new IllegalStateException("Values must be specified");
      }

      if (index == null) {
        index = new HashMap<>();
      }

      return new IndexedDataMap<>(index, values);
    }
  }

  /**
   * Constructs a {@link IndexedDataMap}.
   *
   * @param index the index
   * @param values the values
   */
  private IndexedDataMap(Map<Long, Long> index, AppendOnlyLog<E> values) {
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

  @Override
  public void close() throws Exception {
    try {
      values.close();
    } catch (Exception e) {
      throw new DataCollectionException(e);
    }
  }
}
