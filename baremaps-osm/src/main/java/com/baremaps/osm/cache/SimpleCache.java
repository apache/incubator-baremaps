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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** A {@code Cache} baked by an a {@code Map}. */
public class SimpleCache<K, V> implements Cache<K, V> {

  private final Map<K, V> map;

  /** Constructs a {@code MapCache} baked by a {@code ConcurrentHashMap}. */
  public SimpleCache() {
    this(new ConcurrentHashMap<>());
  }

  /** Constructs a {@code MapCache} baked by a user specified {@code Map}. */
  public SimpleCache(Map<K, V> map) {
    this.map = map;
  }

  /** {@inheritDoc} */
  @Override
  public V get(K key) {
    return map.get(key);
  }

  /** {@inheritDoc} */
  @Override
  public List<V> get(List<K> keys) {
    return keys.stream().map(map::get).collect(Collectors.toList());
  }

  /** {@inheritDoc} */
  @Override
  public void add(K key, V value) {
    map.put(key, value);
  }

  /** {@inheritDoc} */
  @Override
  public void add(List<Entry<K, V>> entries) {
    map.putAll(entries.stream().collect(Collectors.toMap(Entry::key, Entry::value)));
  }

  /** {@inheritDoc} */
  @Override
  public void delete(K key) {
    map.remove(key);
  }

  /** {@inheritDoc} */
  @Override
  public void delete(List<K> keys) {
    keys.forEach(map::remove);
  }
}
