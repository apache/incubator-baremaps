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

  /**
   * Returns the number of values stored in the data map.
   *
   * @return the number of values
   */
  long size();

  /**
   * Returns the value associated with the specified key or null if the key is not present.
   *
   * @param key the key
   * @return the value
   */
  V get(Object key);

  /**
   * Returns the value associated with the specified key or null if the key is not present.
   *
   * @param keys the keys
   * @return the values
   */
  default List<V> getAll(List<K> keys) {
    return Streams.stream(keys).map(this::get).toList();
  }

  /**
   * Associates the specified value with the specified key in the map.
   * 
   * @param key the key
   * @param value the value
   * @return the previous value associated with the key, or null if there was no mapping for the
   *         key.
   */
  V put(K key, V value);

  /**
   * Removes the mapping for the specified key from the map if present.
   *
   * @param key the key
   * @return the previous value associated with the key, or null if there was no mapping for the
   *         key.
   */
  V remove(K key);

  /**
   * Returns true if the map contains a mapping for the specified key.
   *
   * @param key the key
   * @return true if the map contains a mapping for the key
   */
  boolean containsKey(Object key);

  /**
   * Returns true if the map contains a mapping for the specified value.
   *
   * @param value the value
   * @return true if the map contains a mapping for the value
   */
  boolean containsValue(V value);

  /**
   * Clears the map.
   */
  void clear();

  /**
   * Returns true if the map contains no elements.
   * 
   * @return true if the map contains no elements
   */
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
