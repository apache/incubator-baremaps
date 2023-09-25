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

package org.apache.baremaps.database.collection;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.baremaps.database.memory.Memory;
import org.apache.baremaps.database.memory.OffHeapMemory;
import org.apache.baremaps.database.type.FixedSizeDataType;

/**
 * A list that can hold a large number of fixed size data elements.
 *
 * This list is backed by a memory that can be either heap, off-heap, or memory mapped.
 *
 * @param <E> The type of the elements.
 */
public class FixedSizeDataList<E> extends DataList<E> {

  private final FixedSizeDataType<E> dataType;

  private final Memory memory;

  private AtomicLong size;

  /**
   * Constructs a list.
   *
   * @param dataType the data type
   */
  public FixedSizeDataList(FixedSizeDataType<E> dataType) {
    this(dataType, new OffHeapMemory());
  }

  /**
   * Constructs a list.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public FixedSizeDataList(FixedSizeDataType<E> dataType, Memory memory) {
    if (dataType.size() > memory.segmentSize()) {
      throw new DataCollectionException("The segment size is too small for the data type");
    }
    this.dataType = dataType;
    this.memory = memory;
    this.size = new AtomicLong(0);
  }

  private void write(long index, E value) {
    long position = index * dataType.size();
    int segmentIndex = (int) (position / dataType.size());
    int segmentOffset = (int) (position % dataType.size());
    ByteBuffer segment = memory.segment(segmentIndex);
    dataType.write(segment, segmentOffset, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long addIndexed(E value) {
    long index = size.getAndIncrement();
    write(index, value);
    return index;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void set(long index, E value) {
    if (index >= sizeAsLong()) {
      throw new IndexOutOfBoundsException();
    }
    write(index, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E get(long index) {
    long position = index * dataType.size();
    int segmentIndex = (int) (position / dataType.size());
    int segmentOffset = (int) (position % dataType.size());
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return size.get();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    try {
      size.set(0);
      memory.clear();
    } catch (IOException e) {
      throw new DataCollectionException(e);
    }
  }
}
