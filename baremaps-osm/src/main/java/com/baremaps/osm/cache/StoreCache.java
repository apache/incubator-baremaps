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
