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
public class SizedDataList<T> implements DataList<T> {

  private final SizedDataType<T> dataType;

  private final Memory memory;

  private AtomicLong size;

  /**
   * Constructs a list.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public SizedDataList(SizedDataType<T> dataType, Memory memory) {
    if (dataType.size() > memory.segmentSize()) {
      throw new StoreException("The segment size is too small for the data type");
    }
    this.dataType = dataType;
    this.memory = memory;
    this.size = new AtomicLong(0);
  }

  private void write(long index, T value) {
    long position = index * dataType.size();
    int segmentIndex = (int) (position / dataType.size());
    int segmentOffset = (int) (position % dataType.size());
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
    long position = index * dataType.size();
    int segmentIndex = (int) (position / dataType.size());
    int segmentOffset = (int) (position % dataType.size());
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
