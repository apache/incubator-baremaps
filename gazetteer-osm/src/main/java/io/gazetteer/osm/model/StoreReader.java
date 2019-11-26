package io.gazetteer.osm.model;

import java.util.List;

public interface StoreReader<K, V> {

  V get(K key);

  List<V> getAll(List<K> key);

}
