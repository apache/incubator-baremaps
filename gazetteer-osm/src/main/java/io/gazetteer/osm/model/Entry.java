package io.gazetteer.osm.model;

public class Entry<K, V> {

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
