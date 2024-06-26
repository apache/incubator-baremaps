/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.data.collection;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.MemoryException;
import org.apache.baremaps.data.memory.OffHeapMemory;
import org.apache.baremaps.data.type.FixedSizeDataType;

/**
 * A {@link DataList} that can hold a large number of fixed-size memory-aligned data elements. This
 * data list is backed by a memory that can be either heap, off-heap, or memory mapped.
 *
 * @param <E> The type of the elements.
 */
public class MemoryAlignedDataList<E> implements DataList<E> {

  private final FixedSizeDataType<E> dataType;

  private final Memory<?> memory;

  private final int valueShift;

  private final long segmentShift;

  private final long segmentMask;

  private AtomicLong size;

  /**
   * Constructs a {@link MemoryAlignedDataList}.
   *
   * @param dataType the data type
   */
  public MemoryAlignedDataList(FixedSizeDataType<E> dataType) {
    this(dataType, new OffHeapMemory());
  }

  /**
   * Constructs a {@link MemoryAlignedDataList}.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public MemoryAlignedDataList(FixedSizeDataType<E> dataType, Memory<?> memory) {
    if (dataType.size() > memory.segmentSize()) {
      throw new DataCollectionException("The segment size is too small for the data type");
    }
    if ((dataType.size() & -dataType.size()) != dataType.size()) {
      throw new IllegalArgumentException("The data type size must be a fixed power of 2");
    }
    if (memory.segmentSize() % dataType.size() != 0) {
      throw new DataCollectionException("The segment size and data type size must be aligned");
    }
    this.dataType = dataType;
    this.memory = memory;
    this.valueShift = (int) (Math.log(dataType.size()) / Math.log(2));
    this.segmentShift = memory.segmentShift();
    this.segmentMask = memory.segmentMask();
    this.size = new AtomicLong(0);
  }

  private void write(long index, E value) {
    long position = index << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    dataType.write(segment, segmentOffset, value);
  }

  /** {@inheritDoc} */
  public long addIndexed(E value) {
    long index = size.getAndIncrement();
    write(index, value);
    return index;
  }

  /** {@inheritDoc} */
  public void set(long index, E value) {
    if (index >= size.get()) {
      size.set(index + 1);
    }
    write(index, value);
  }

  /** {@inheritDoc} */
  public E get(long index) {
    long position = index << valueShift;
    int segmentIndex = (int) (position >> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }

  /** {@inheritDoc} */
  @Override
  public long size() {
    return size.get();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    size.set(0);
    try {
      memory.clear();
    } catch (IOException e) {
      throw new MemoryException(e);
    }
  }

}
