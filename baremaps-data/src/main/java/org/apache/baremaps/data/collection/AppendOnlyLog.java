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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.OffHeapMemory;
import org.apache.baremaps.data.type.DataType;

/**
 * A log of records backed by a {@link DataType} and a {@link Memory}. Elements are appended to the
 * log and can be accessed by their position in the {@link Memory}. Appending elements to the log is
 * thread-safe.
 *
 * @param <E> The type of the data.
 */
public class AppendOnlyLog<E> implements DataCollection<E> {

  private final DataType<E> dataType;
  private final Memory<?> memory;
  private final long segmentSize;
  private long offset;
  private long size;

  private final Lock lock = new ReentrantLock();

  /**
   * Constructs an {@link AppendOnlyLog}.
   *
   * @param dataType the data type
   */
  public AppendOnlyLog(DataType<E> dataType) {
    this(dataType, new OffHeapMemory());
  }

  /**
   * Constructs an append only log.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public AppendOnlyLog(DataType<E> dataType, Memory<?> memory) {
    this.dataType = dataType;
    this.memory = memory;
    this.segmentSize = memory.segmentSize();
    this.offset = Long.BYTES;
    this.size = memory.segment(0).getLong(0);
  }

  /**
   * Persists the current size to memory.
   */
  public void persistSize() {
    memory.segment(0).putLong(0, size);
  }

  /**
   * Appends the value to the log and returns its position in the memory.
   *
   * @param value the value
   * @return the position of the value in the memory.
   */
  public long addPositioned(E value) {
    int valueSize = dataType.size(value);
    if (valueSize > segmentSize) {
      throw new DataCollectionException("The value is too big to fit in a segment");
    }

    lock.lock();
    long position = offset;
    long segmentIndex = position / segmentSize;
    long segmentOffset = position % segmentSize;

    if (segmentOffset + valueSize > segmentSize) {
      segmentOffset = 0;
      segmentIndex = segmentIndex + 1;
      position = segmentIndex * segmentSize;
    }
    offset = position + valueSize;
    this.size++;
    lock.unlock();

    ByteBuffer segment = memory.segment((int) segmentIndex);
    dataType.write(segment, (int) segmentOffset, value);

    return position;
  }

  /**
   * Returns a values at the specified position in the memory.
   *
   * @param position the position of the value
   * @return the value
   */
  public E getPositioned(long position) {
    long segmentIndex = position / segmentSize;
    long segmentOffset = position % segmentSize;
    ByteBuffer buffer = memory.segment((int) segmentIndex);
    return dataType.read(buffer, (int) segmentOffset);
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(E e) {
    addPositioned(e);
    return true;
  }

  /** {@inheritDoc} */
  public long size() {
    return size;
  }

  /** {@inheritDoc} */
  public void clear() {
    try {
      memory.clear();
      this.size = 0;
      persistSize();
    } catch (IOException e) {
      throw new DataCollectionException(e);
    }
  }

  /**
   * Returns an iterator over the values of the log, starting at the beginning of the log. The
   * iterator allows to get the current position in the memory.
   * 
   * @return an iterator over the values of the log
   */
  @Override
  public AppendOnlyLogIterator iterator() {
    return new AppendOnlyLogIterator(size);
  }

  /**
   * An iterator over the values of the log that can be used to iterate over the values of the log
   * and to get the current position in the memory.
   */
  public class AppendOnlyLogIterator implements Iterator<E> {

    private final long size;

    private long index;

    private long position;

    private AppendOnlyLogIterator(long size) {
      this.size = size;
      index = 0;
      position = Long.BYTES;
    }

    @Override
    public boolean hasNext() {
      return index < size;
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      long segmentIndex = position / segmentSize;
      long segmentOffset = position % segmentSize;

      ByteBuffer segment = memory.segment((int) segmentIndex);

      int valueSize;
      try {
        valueSize = dataType.size(segment, (int) segmentOffset);
      } catch (IndexOutOfBoundsException e) {
        valueSize = 0;
      }

      if (segmentOffset + valueSize > segmentSize || valueSize == 0) {
        segmentIndex = segmentIndex + 1;
        segmentOffset = 0;
        position = segmentIndex * segmentSize;
        segment = memory.segment((int) segmentIndex);
        valueSize = dataType.size(segment, (int) segmentOffset);
      }
      position += valueSize;
      index++;

      return dataType.read(segment, (int) segmentOffset);
    }

    public long getPosition() {
      return position;
    }

  }
}
