package com.baremaps.store.type;

public interface FixedSizeDataType<T> extends DataType<T> {

  default int size() {
    return size(null);
  }

}
