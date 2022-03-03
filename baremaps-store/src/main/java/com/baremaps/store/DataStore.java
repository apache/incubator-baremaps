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

/**
 * A data store backed by a {@link DataType} and a {@link Memory}. Data is appended to the store and
 * can be accessed by its position in the {@link Memory}.
 */
public class DataStore<T> {

  private final DataType<T> dataType;
  private final Memory memory;
  private final long segmentBytes;
  private long offset;
  private long size;

  private Lock lock = new ReentrantLock();

  /**
   * Constructs a data store.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public DataStore(DataType<T> dataType, Memory memory) {
    this.dataType = dataType;
    this.memory = memory;
    this.segmentBytes = memory.segmentSize();
    this.offset = 0;
    this.size = 0;
  }

  /**
   * Appends a value to the data store and returns its position in the memory.
   *
   * @param value the value
   * @return the position of the value in the memory.
   */
  public long add(T value) {
    int size = dataType.size(value);
    if (size > segmentBytes) {
      throw new RuntimeException("The value is too big to fit in a segment");
    }

    lock.lock();
    long position = offset;
    long segmentIndex = position / segmentBytes;
    long segmentOffset = position % segmentBytes;
    if (segmentOffset + size > segmentBytes) {
      segmentOffset = 0;
      segmentIndex = segmentIndex + 1;
      position = segmentIndex * segmentBytes;
    }
    offset = position + size;
    this.size++;
    lock.unlock();

    ByteBuffer segment = memory.segment((int) segmentIndex);
    dataType.write(segment, (int) segmentOffset, value);

    return position;
  }

  /**
   * Returns a values by its position in memory.
   *
   * @param position the position of the value
   * @return the value
   */
  public T get(long position) {
    long segmentIndex = position / segmentBytes;
    long segmentOffset = position % segmentBytes;
    ByteBuffer buffer = memory.segment((int) segmentIndex);
    return dataType.read(buffer, (int) segmentOffset);
  }

  /**
   * Returns the number of values stored in the data store.
   *
   * @return the number of values
   */
  public long size() {
    return size;
  }
}
