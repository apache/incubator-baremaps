package com.baremaps.store.type;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Pair)) {
      return false;
    }

    Pair<?, ?> pair = (Pair<?, ?>) o;

    if (!Objects.equals(left, pair.left)) {
      return false;
    }
    
    return Objects.equals(right, pair.right);
  }

  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }
}
