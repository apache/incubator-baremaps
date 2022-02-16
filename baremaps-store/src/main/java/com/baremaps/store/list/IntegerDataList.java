package com.baremaps.store.list;

import com.baremaps.store.memory.Memory;
import com.baremaps.store.type.IntegerDataType;

public class IntegerDataList extends FixedSizeDataList<Integer> {

  public IntegerDataList(Memory memory) {
    super(new IntegerDataType(), memory);
  }
}
