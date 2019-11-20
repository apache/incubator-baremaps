package io.gazetteer.osm.model;

import java.util.List;

public interface Store<K, V> {

  V get(K key);

  List<V> getAll(List<K> key);

  void put(K key, V values);

  void putAll(List<Entry<K, V>> entries);

  void delete(K key);

  void deleteAll(List<K> keys);

  void importAll(List<Entry<K, V>> values);

}
