package com.baremaps.store;

import java.util.function.IntFunction;

public class SmallLongDataList implements DataList<Long> {

  private static final int BITS = 31;
  private static final long MASK = (1L << BITS) - 1L;
  private final IntegerDataList[] ints = new IntegerDataList[10];
  private long numWritten = 0;

  public SmallLongDataList(IntFunction<IntegerDataList> supplier) {
    for (int i = 0; i < ints.length; i++) {
      ints[i] = supplier.apply(i);
    }
  }

  @Override
  public long add(Long value) {
    int block = (int) (value >>> BITS);
    int offset = (int) (value & MASK);
    ints[block].add(offset);
    return ++numWritten;
  }

  @Override
  public Long get(long index) {
    for (int i = 0; i < ints.length; i++) {
      IntegerDataList slab = ints[i];
      long size = slab.size();
      if (index < slab.size()) {
        return slab.get(index) + (((long) i) << BITS);
      }
      index -= size;
    }
    throw new IndexOutOfBoundsException("index: " + index + " size: " + numWritten);
  }

}
