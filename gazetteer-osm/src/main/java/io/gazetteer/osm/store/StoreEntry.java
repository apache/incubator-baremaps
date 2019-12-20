package io.gazetteer.osm.store;

public class StoreEntry<K, V> {

  private final K key;
  private final V value;

  public StoreEntry(K key, V value) {
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
