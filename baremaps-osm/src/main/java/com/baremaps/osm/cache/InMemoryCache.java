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
