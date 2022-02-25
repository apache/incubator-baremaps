package com.baremaps.store.type;

import java.nio.ByteBuffer;

public class PairDataType<L, R> implements AlignedDataType<Pair<L, R>> {

  private final AlignedDataType<L> left;
  private final AlignedDataType<R> right;

  public PairDataType(AlignedDataType<L> left, AlignedDataType<R> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public int size(Pair<L, R> value) {
    return left.size() + right.size();
  }

  @Override
  public void write(ByteBuffer buffer, int position, Pair<L, R> value) {
    left.write(buffer, position, value.left());
    right.write(buffer, position + left.size(), value.right());
  }

  @Override
  public Pair<L, R> read(ByteBuffer buffer, int position) {
    L l = left.read(buffer, position);
    R r = right.read(buffer, position + left.size());
    return new Pair<>(l, r);
  }

}
