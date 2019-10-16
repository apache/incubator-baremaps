package io.gazetteer.osm.data;

public class VariableSizeObjectMap<T> {

  private final FixedSizeObjectMap<Long> keys;

  private final VariableSizeObjectStore<T> values;

  public VariableSizeObjectMap(FixedSizeObjectMap<Long> keys, VariableSizeObjectStore<T> values) {
    this.keys = keys;
    this.values = values;
  }

  public T get(long position) {
    Long p = keys.get(position);
    if (p == null) {
      return null;
    }
    return values.read(p);
  }

  public void set(long position, T value) {
    keys.set(position, values.write(value));
  }

}
