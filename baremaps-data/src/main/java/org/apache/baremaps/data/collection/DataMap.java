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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * A {@code DataMap<E>} maps keys to values. It is similar to a {@link java.util.Map<K, V> Map}, but
 * can hold up to {@link Long#MAX_VALUE} entries.
 *
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
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
   * Returns the values associated with the specified keys or null if the key is not present.
   *
   * @param keys the keys
   * @return the values
   */
  default Iterable<V> getAll(Iterable<K> keys) {
    List<V> values = new ArrayList<>();
    keys.forEach(key -> values.add(get(key)));
    return values;
  }

  /**
   * Associates the specified value with the specified key in the data map.
   *
   * @param key the key
   * @param value the value
   * @return the previous value associated with the key, or null if there was no mapping for the key
   */
  V put(K key, V value);

  /**
   * Associates the specified values with the specified keys in the data map.
   *
   * @param entries the entries
   */
  default void putAll(Iterable<Entry<K, V>> entries) {
    entries.forEach(entry -> put(entry.getKey(), entry.getValue()));
  }

  /**
   * Returns true if the data map contains a mapping for the specified key.
   *
   * @param key the key
   * @return true if the data map contains a mapping for the key
   */
  boolean containsKey(Object key);

  /**
   * Returns true if the data map contains a mapping for the specified value.
   *
   * @param value the value
   * @return true if the data map contains a mapping for the value
   */
  boolean containsValue(V value);

  /**
   * Clears the data map.
   */
  void clear();

  /**
   * Returns true if the data map contains no elements.
   * 
   * @return true if the data map contains no elements
   */
  default boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns an iterator over the keys of the data map.
   *
   * @return an iterator
   */
  Iterator<K> keyIterator();

  /**
   * Returns an iterable over the keys of the data map.
   *
   * @return an iterable
   */
  default Iterable<K> keys() {
    return this::keyIterator;
  }

  /**
   * Returns an iterator over the values of the data map.
   *
   * @return an iterator
   */
  Iterator<V> valueIterator();

  /**
   * Returns an iterable over the values of the data map.
   *
   * @return an iterable
   */
  default Iterable<V> values() {
    return this::valueIterator;
  }

  /**
   * Returns an iterator over the entries of the data map.
   *
   * @return an iterator
   */
  Iterator<Entry<K, V>> entryIterator();

  /**
   * Returns an iterable over the entries of the data map.
   *
   * @return an iterable
   */
  default Iterable<Entry<K, V>> entries() {
    return this::entryIterator;
  }

  /**
   * Performs the given action for each entry in the data map.
   *
   * @param action the action to be performed
   */
  default void forEach(BiConsumer<? super K, ? super V> action) {
    for (Entry<K, V> entry : entries()) {
      action.accept(entry.getKey(), entry.getValue());
    }
  }

}
