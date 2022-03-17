package com.baremaps.collection.sort;

import com.baremaps.collection.DataList;

/**
 * A wrapper on top of a {@link DataList} which keeps the last data record in memory.
 *
 * @param <T>
 */
final class DataStack<T> {

  private DataList<T> list;

  private Long index = 0l;

  private T cache;

  public DataStack(DataList<T> list) {
    this.list = list;
    reload();
  }

  public void close() {
    // do nothing
  }

  public boolean empty() {
    return this.index > list.size();
  }

  public T peek() {
    return this.cache;
  }

  public T pop() {
    T answer = peek(); // make a copy
    reload();
    return answer;
  }

  private void reload() {
    this.cache = this.list.get(index);
    index++;
  }

}
