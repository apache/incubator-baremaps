package com.baremaps.store;

import com.baremaps.store.memory.Memory;
import com.baremaps.store.type.IntegerDataType;

public class LongDataList extends AlignedDataList<Integer> {

  public LongDataList(Memory memory) {
    super(new IntegerDataType(), memory);
  }
}
