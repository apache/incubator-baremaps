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

import com.baremaps.store.LongDataMap;
import java.util.List;
import java.util.stream.Collectors;

public class StoreCache<V> implements Cache<Long, V> {

  private final LongDataMap<V> map;

  public StoreCache(LongDataMap<V> map) {
    this.map = map;
  }

  @Override
  public V get(Long key) throws CacheException {
    return map.get(key);
  }

  @Override
  public List<V> get(List<Long> keys) throws CacheException {
    return keys.stream().map(map::get).collect(Collectors.toList());
  }

  @Override
  public void put(Long key, V value) throws CacheException {
    map.put(key, value);
  }

  @Override
  public void put(List<Entry<Long, V>> entries) throws CacheException {
    entries.stream().forEach(entry -> map.put(entry.key(), entry.value()));
  }

  @Override
  public void delete(Long key) throws CacheException {
    // not supported
  }

  @Override
  public void delete(List<Long> keys) throws CacheException {
    // not supported
  }
}
