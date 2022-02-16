package com.baremaps.store.list;

import com.baremaps.store.memory.Memory;
import com.baremaps.store.type.IntegerDataType;

public class LongDataList extends FixedSizeDataList<Integer> {

  public LongDataList(Memory memory) {
    super(new IntegerDataType(), memory);
  }
}
