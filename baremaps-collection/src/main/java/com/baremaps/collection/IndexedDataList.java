package com.baremaps.collection;

import java.io.IOException;

public class IndexedDataList<T> implements DataList<T> {

  private final LongList index;

  private final DataStore<T> store;

  public IndexedDataList(LongList index, DataStore<T> store) {
    this.index = index;
    this.store = store;
  }

  @Override
  public long add(T value) {
    return index.add(store.add(value));
  }

  @Override
  public void add(long idx, T value) {
    index.add(idx, store.add(value));
  }

  @Override
  public T get(long idx) {
    return store.get(index.get(idx));
  }

  @Override
  public long size() {
    return index.size();
  }

  @Override
  public void close() throws IOException {
    index.close();
    store.close();
  }

  @Override
  public void clean() throws IOException {
    index.clean();
    store.clean();
  }
}
