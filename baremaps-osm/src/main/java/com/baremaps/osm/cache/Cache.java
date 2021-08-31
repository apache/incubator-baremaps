/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.osm.cache;

import java.util.List;

/**
 * Provides an interface to a cache.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public interface Cache<K, V> {

  /**
   * Returns the cached value for the specified key.
   *
   * @param key the key
   * @return the value
   * @throws CacheException
   */
  V get(K key) throws CacheException;

  /**
   * Returns the cached values for the specified keys.
   *
   * @param keys the keys
   * @return the values
   * @throws CacheException
   */
  List<V> get(List<K> keys) throws CacheException;

  /**
   * Adds the specified key-value pair to the cache.
   *
   * @param key the key
   * @param value the value
   * @throws CacheException
   */
  void add(K key, V value) throws CacheException;

  /**
   * Adds the specified key-value pairs to the cache.
   *
   * @param entries the key-value pairs
   * @throws CacheException
   */
  void add(List<Entry<K, V>> entries) throws CacheException;

  /**
   * Deletes the cached value for the specified key.
   *
   * @param key the key
   * @throws CacheException
   */
  void delete(K key) throws CacheException;

  /**
   * Deletes the cached values for the specified keys.
   *
   * @param keys the keys
   * @throws CacheException
   */
  void delete(List<K> keys) throws CacheException;

  /**
   * A cache entry.
   *
   * @param <K> the type of the key
   * @param <V> the type of the value
   */
  class Entry<K, V> {

    private final K key;
    private final V value;

    public Entry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    /**
     * Returns the key.
     *
     * @return the key
     */
    public K key() {
      return key;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public V value() {
      return value;
    }
  }
}
