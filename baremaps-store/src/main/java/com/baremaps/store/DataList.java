package com.baremaps.store;

public interface DataList<T> {

  long add(T value);

  T get(long index);

}
