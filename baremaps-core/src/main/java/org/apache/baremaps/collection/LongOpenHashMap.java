/*
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

package org.apache.baremaps.collection;



import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.baremaps.collection.store.AppendOnlyCollection;

/**
 * A map of data backed by a {@link AppendOnlyCollection} and whose keys are stored in an
 * {@link Long2LongOpenHashMap}.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class LongOpenHashMap<T> implements LongMap<T>, Map<Long, T> {

  private final Map<Long, Long> index;
  private final AppendOnlyCollection<T> store;

  public LongOpenHashMap(AppendOnlyCollection<T> values) {
    this.index = new Long2LongOpenHashMap();
    this.store = values;
  }

  public LongOpenHashMap(Map<Long, Long> index, AppendOnlyCollection<T> store) {
    this.index = index;
    this.store = store;
  }

  @Override
  public void put(long key, T value) {
    index.put(key, store.append(value));
  }

  @Override
  public T get(long key) {
    return store.read(index.get(key));
  }

  @Override
  public int size() {
    return index.size();
  }

  @Override
  public boolean isEmpty() {
    return index.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return index.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T get(Object key) {
    var position = index.get(key);
    return store.read(position);
  }

  @Override
  public T put(Long key, T value) {
    var previous = get(key);
    var position = store.append(value);
    index.put(key, position);
    return previous;
  }

  @Override
  public T remove(Object key) {
    var previous = get(key);
    index.remove(key);
    return previous;
  }

  @Override
  public void putAll(Map<? extends Long, ? extends T> m) {
    m.forEach(this::put);
  }

  @Override
  public void clear() {
    index.clear();
  }

  @Override
  public Set<Long> keySet() {
    return index.keySet();
  }

  @Override
  public Collection<T> values() {
    return index.values().stream().map(store::read).collect(Collectors.toList());
  }

  @Override
  public Set<Entry<Long, T>> entrySet() {
    return index.entrySet().stream().map(entry -> {
      var key = entry.getKey();
      var value = store.read(entry.getValue());
      return Map.entry(key, value);
    }).collect(Collectors.toSet());
  }
}
