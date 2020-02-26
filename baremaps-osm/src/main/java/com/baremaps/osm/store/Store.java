package com.baremaps.osm.store;

import java.util.List;

public interface Store<K, V>  {

  V get(K key);

  List<V> getAll(List<K> keys);

  void put(K key, V values);

  void putAll(List<Entry<K, V>> entries);

  void delete(K key);

  void deleteAll(List<K> keys);

  void importAll(List<Entry<K, V>> values);

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
