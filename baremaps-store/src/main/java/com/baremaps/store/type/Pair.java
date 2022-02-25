package com.baremaps.store.type;

public class Pair<L, R> {

  private final L left;

  private final R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L left() {
    return left;
  }

  public R right() {
    return right;
  }

}
