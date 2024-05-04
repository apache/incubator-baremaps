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
import java.util.*;
import java.util.Map.Entry;

/**
 * An abstract map of data elements that can hold a large number of elements.
 *
 * @param <V> The type of the elements.
 */
public interface DataMap<K, V> {

  long size();

  V get(Object key);

  V put(K key, V value);

  V remove(Object key);

  void clear();

  boolean containsKey(Object key);

  boolean containsValue(Object value);

  default void putAll(Map<? extends K, ? extends V> m) {
    m.forEach(this::put);
  }

  /**
   * Returns the value associated with the specified key or null if the key is not present.
   *
   * @param keys the keys
   * @return the values
   */
  default List<V> getAll(List<K> keys) {
    return Streams.stream(keys).map(this::get).toList();
  }

  /** {@inheritDoc} */
  default boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns an iterator over the keys of the map.
   *
   * @return an iterator
   */
  Iterator<K> keyIterator();

  /**
   * Returns an iterator over the values of the map.
   *
   * @return an iterator
   */
  Iterator<V> valueIterator();

  /**
   * Returns an iterator over the entries of the map.
   *
   * @return an iterator
   */
  Iterator<Entry<K, V>> entryIterator();

  /** {@inheritDoc} */
  default Set<Entry<K, V>> entrySet() {
    int size = (int) size();
    return new AbstractSet<>() {

      @Override
      public Iterator<Entry<K, V>> iterator() {
        return entryIterator();
      }

      @Override
      public int size() {
        return size;
      }
    };
  }

}
