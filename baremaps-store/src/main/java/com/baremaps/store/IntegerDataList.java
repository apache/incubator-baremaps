package com.baremaps.store;

import com.baremaps.store.memory.Memory;
import com.baremaps.store.type.IntegerDataType;

public class IntegerDataList extends AlignedDataList<Integer> {

  public IntegerDataList(Memory memory) {
    super(new IntegerDataType(), memory);
  }

}
