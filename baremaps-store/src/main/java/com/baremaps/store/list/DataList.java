package com.baremaps.store.list;

public interface DataList<T> {

  long add(T value);

  T get(long position);

}
