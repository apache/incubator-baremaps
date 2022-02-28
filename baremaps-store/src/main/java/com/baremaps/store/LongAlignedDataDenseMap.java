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

import com.baremaps.store.memory.Memory;
import com.baremaps.store.type.AlignedDataType;
import java.nio.ByteBuffer;

public class LongAlignedDataDenseMap<T> implements LongDataMap<T> {

  private final AlignedDataType<T> dataType;

  private final Memory memory;

  private final int valueShift;

  private final long segmentShift;

  private final long segmentMask;

  public LongAlignedDataDenseMap(AlignedDataType<T> dataType, Memory memory) {
    if (dataType.size() > memory.segmentSize()) {
      throw new RuntimeException("The values are too big");
    }
    if (memory.segmentSize() % dataType.size() != 0) {
      throw new RuntimeException("The segment size and data type size must be aligned");
    }
    this.dataType = dataType;
    this.memory = memory;
    this.valueShift = (int) (Math.log(dataType.size()) / Math.log(2));
    ;
    this.segmentShift = memory.segmentShift();
    this.segmentMask = memory.segmentMask();
  }

  @Override
  public void put(long key, T value) {
    long position = key << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    dataType.write(segment, segmentOffset, value);
  }

  @Override
  public T get(long key) {
    long position = key << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }
}
