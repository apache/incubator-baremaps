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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.baremaps.collection.memory.Memory;
import org.apache.baremaps.collection.memory.OffHeapMemory;
import org.apache.baremaps.collection.type.DataType;

/**
 * A data log backed by a {@link DataType} and a {@link Memory}. Elements are appended to the store
 * and can be accessed by their position in the {@link Memory}.
 */
public class AppendOnlyBuffer<T> extends DataList<T> {

  private final DataType<T> dataType;
  private final Memory memory;
  private final long segmentSize;
  private long offset;
  private long size;

  private Lock lock = new ReentrantLock();

  /**
   * Constructs a data list.
   *
   * @param dataType the data type
   */
  public AppendOnlyBuffer(DataType<T> dataType) {
    this(dataType, new OffHeapMemory());
  }

  /**
   * Constructs a data list.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public AppendOnlyBuffer(DataType<T> dataType, Memory memory) {
    this.dataType = dataType;
    this.memory = memory;
    this.segmentSize = memory.segmentSize();
    this.offset = 0;
    this.size = 0;
  }

  /**
   * Appends a value to the data store and returns its position in the memory.
   *
   * @param value the value
   * @return the position of the value in the memory.
   */
  @Override
  public long append(T value) {
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
   * Returns a values by its position in memory.
   *
   * @param position the position of the value
   * @return the value
   */
  public T get(long position) {
    long segmentIndex = position / segmentSize;
    long segmentOffset = position % segmentSize;
    ByteBuffer buffer = memory.segment((int) segmentIndex);
    return dataType.read(buffer, (int) segmentOffset);
  }

  /**
   * Returns the number of values stored in the data store.
   *
   * @return the number of values
   */
  public long sizeAsLong() {
    return size;
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    try {
      memory.clear();
    } catch (IOException e) {
      throw new DataCollectionException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(T value) {
    append(value);
    return true;
  }

  @Override
  public void set(long index, T value) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return (int) Math.min(size, Integer.MAX_VALUE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<T> iterator() {
    final long size = sizeAsLong();
    return new Iterator<>() {

      private long index = 0;

      private long position = 0;

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        long segmentIndex = position / segmentSize;
        long segmentOffset = position % segmentSize;
        ByteBuffer buffer = memory.segment((int) segmentIndex);
        position += dataType.size(buffer, (int) segmentOffset);
        index++;
        return dataType.read(buffer, (int) segmentOffset);
      }
    };
  }
}
