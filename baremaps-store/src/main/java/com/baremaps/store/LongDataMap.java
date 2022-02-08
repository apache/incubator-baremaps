package com.baremaps.store;

public interface LongDataMap<T> {

  void put(long key, T value);

  T get(long key);

}
