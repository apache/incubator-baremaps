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

public interface Cache<K, V> {

  V get(K key) throws CacheException;

  List<V> get(List<K> keys) throws CacheException;

  void add(K key, V value) throws CacheException;

  void add(List<Entry<K, V>> entries) throws CacheException;

  void delete(K key) throws CacheException;

  void deleteAll(List<K> keys) throws CacheException;

  class Entry<K, V> {

    private final K key;
    private final V value;

    public Entry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K key() {
      return key;
    }

    public V value() {
      return value;
    }
  }
}
