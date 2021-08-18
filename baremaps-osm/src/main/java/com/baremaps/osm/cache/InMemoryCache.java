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

public abstract class InMemoryCache<K, V> implements Cache<K, V> {

  private final Map<K, V> map = new ConcurrentHashMap<>();

  @Override
  public V get(K key) {
    return map.get(key);
  }

  @Override
  public List<V> get(List<K> keys) {
    return keys.stream().map(map::get).collect(Collectors.toList());
  }

  @Override
  public void add(K key, V value) {
    map.put(key, value);
  }

  @Override
  public void add(List<Entry<K, V>> entries) {
    map.putAll(entries.stream().collect(Collectors.toMap(Entry::key, Entry::value)));
  }

  @Override
  public void delete(K key) {
    map.remove(key);
  }

  @Override
  public void deleteAll(List<K> keys) {
    keys.forEach(map::remove);
  }
}
