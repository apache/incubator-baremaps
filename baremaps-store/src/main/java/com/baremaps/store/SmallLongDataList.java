/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.store;

import java.util.function.IntFunction;

/**
 * A list of longs that uses 4 bytes to represent each long by using a list of integers. Only
 * suitable for values less than ~20 billion (i.e. OSM node IDs)
 *
 * <p>This code has been adapted from Planetiler (Apache license).
 * <p>Copyright (c) Planetiler.
 */
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

  /** {@inheritDoc} */
  @Override
  public long add(Long value) {
    int block = (int) (value >>> BITS);
    int offset = (int) (value & MASK);
    ints[block].add(offset);
    return ++numWritten;
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public long size() {
    return numWritten;
  }
}
