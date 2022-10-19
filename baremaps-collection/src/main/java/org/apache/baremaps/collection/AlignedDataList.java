/*
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

package org.apache.baremaps.collection;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.baremaps.collection.memory.Memory;
import org.apache.baremaps.collection.type.SizedDataType;

/**
 * A list of data backed by a {@link SizedDataType} and a {@link Memory}.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 *
 * @param <T>
 */
public class AlignedDataList<T> implements DataList<T> {

  private final SizedDataType<T> dataType;

  private final Memory memory;

  private final int valueShift;

  private final long segmentShift;

  private final long segmentMask;

  private AtomicLong size;

  /**
   * Constructs a list.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public AlignedDataList(SizedDataType<T> dataType, Memory memory) {
    if (dataType.size() > memory.segmentSize()) {
      throw new StoreException("The segment size is too small for the data type");
    }
    if (memory.segmentSize() % dataType.size() != 0) {
      throw new StoreException("The segment size and data type size must be aligned");
    }
    this.dataType = dataType;
    this.memory = memory;
    this.valueShift = (int) (Math.log(dataType.size()) / Math.log(2));
    this.segmentShift = memory.segmentShift();
    this.segmentMask = memory.segmentMask();
    this.size = new AtomicLong(0);
  }

  private void write(long index, T value) {
    long position = index << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    dataType.write(segment, segmentOffset, value);
  }

  /** {@inheritDoc} */
  public long add(T value) {
    long index = size.getAndIncrement();
    write(index, value);
    return index;
  }

  @Override
  public void add(long index, T value) {
    if (index >= size.get()) {
      throw new IndexOutOfBoundsException();
    }
    write(index, value);
  }

  /** {@inheritDoc} */
  public T get(long index) {
    long position = index << valueShift;
    int segmentIndex = (int) (position >> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }

  /** {@inheritDoc} */
  public long size() {
    return size.get();
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    memory.close();
  }

  /** {@inheritDoc} */
  @Override
  public void clean() throws IOException {
    memory.clean();
  }
}
