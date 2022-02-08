package com.baremaps.store;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Map;

public class LongDataOpenHashMap<T> implements LongDataMap<T> {

  private final Map<Long, Long> map;
  private final DataStore<T> store;

  public LongDataOpenHashMap(DataStore<T> values) {
    this.map = new Long2LongOpenHashMap();
    this.store = values;
  }

  @Override
  public void put(long key, T value) {
    map.put(key, store.add(value));
  }

  @Override
  public T get(long key) {
    return store.get(map.get(key));
  }
}
