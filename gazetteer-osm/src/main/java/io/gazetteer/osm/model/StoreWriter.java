package io.gazetteer.osm.model;

import java.util.List;

public interface StoreWriter<K, V> {

  void put(K key, V values);

  void putAll(List<StoreEntry<K, V>> entries);

  void delete(K key);

  void deleteAll(List<K> keys);

  void importAll(List<StoreEntry<K, V>> values);

}
