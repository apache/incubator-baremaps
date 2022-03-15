package com.baremaps.collection.sort;

import com.baremaps.collection.AlignedDataList;
import java.io.IOException;

public final class AlignedDataStack {

  private AlignedDataList<Long> fbr;

  private Long index = 0l;

  private Long cache;

  public AlignedDataStack(AlignedDataList<Long> fbr) {
    this.fbr = fbr;
    reload();
  }

  public void close() {
    // do nothing
  }

  public boolean empty() {
    return this.index> fbr.size();
  }

  public Long peek() {
    return this.cache;
  }

  public Long pop() {
    Long answer = peek(); // make a copy
    reload();
    return answer;
  }

  private void reload() {
    this.cache = this.fbr.get(index++);
  }


}
