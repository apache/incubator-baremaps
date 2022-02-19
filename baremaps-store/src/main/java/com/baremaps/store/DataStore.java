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
import com.baremaps.store.type.DataType;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataStore<T> {

  private final DataType<T> dataType;
  private final Memory memory;
  private final int segmentBytes;
  private long offset;
  private long size;

  private Lock lock = new ReentrantLock();

  public DataStore(DataType<T> dataType, Memory memory) {
    this.dataType = dataType;
    this.memory = memory;
    this.segmentBytes = memory.segmentSize();
    this.offset = 0;
    this.size = 0;
  }

  public long add(T value) {
    int size = dataType.size(value);
    if (size > segmentBytes) {
      throw new RuntimeException("The value is too big to fit in a segment");
    }

    lock.lock();
    long position = offset;
    int segmentIndex = (int) (position / segmentBytes);
    int segmentOffset = (int) (position % segmentBytes);
    if (segmentOffset + size > segmentBytes) {
      segmentOffset = 0;
      segmentIndex = segmentIndex + 1;
      position = segmentIndex * segmentBytes;
    }
    offset = position + size;
    this.size++;
    lock.unlock();

    ByteBuffer segment = memory.segment(segmentIndex);
    dataType.write(segment, segmentOffset, value);

    return position;
  }

  public T get(long position) {
    int segmentIndex = (int) position / segmentBytes;
    int segmentOffset = (int) position % segmentBytes;
    ByteBuffer buffer = memory.segment(segmentIndex);
    return dataType.read(buffer, segmentOffset);
  }

  public long bytes() {
    return offset;
  }

  public long size() {
    return size;
  }
}
