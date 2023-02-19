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
 * A buffer of elements backed by a {@link DataType} and a {@link Memory}. Elements are appended to
 * the buffer and can be accessed by their position in the {@link Memory}. Appending elements to the
 * buffer is thread-safe.
 *
 * @param <E> The type of the data.
 */
public class AppendOnlyBuffer<E> extends DataCollection<E> {

  private final DataType<E> dataType;
  private final Memory memory;
  private final long segmentSize;
  private long offset;
  private long size;

  private Lock lock = new ReentrantLock();

  /**
   * Constructs an append only buffer.
   *
   * @param dataType the data type
   */
  public AppendOnlyBuffer(DataType<E> dataType) {
    this(dataType, new OffHeapMemory());
  }

  /**
   * Constructs an append only buffer.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public AppendOnlyBuffer(DataType<E> dataType, Memory memory) {
    this.dataType = dataType;
    this.memory = memory;
    this.segmentSize = memory.segmentSize();
    this.offset = Long.BYTES;
    this.size = memory.segment(0).getLong(0);
  }

  /**
   * Appends the value to the buffer and returns its position in the memory.
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
   * {@inheritDoc}
   */
  @Override
  public boolean add(E value) {
    addPositioned(value);
    return true;
  }

  /**
   * Returns a values at the specified position in the memory.
   *
   * @param position the position of the value
   * @return the value
   */
  public E read(long position) {
    long segmentIndex = position / segmentSize;
    long segmentOffset = position % segmentSize;
    ByteBuffer buffer = memory.segment((int) segmentIndex);
    return dataType.read(buffer, (int) segmentOffset);
  }

  /** {@inheritDoc} */
  public long sizeAsLong() {
    return size;
  }

  public void close() {
    memory.segment(0).putLong(0, size);
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
  public BufferIterator iterator() {
    final long size = sizeAsLong();
    return new BufferIterator(size);
  }

  public class BufferIterator implements Iterator<E> {

    private final long size;
    private long index;

    private long position;

    public BufferIterator(long size) {
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

      int size;
      try {
        size = dataType.size(segment, (int) segmentOffset);
      } catch (IndexOutOfBoundsException e) {
        size = 0;
      }

      if (segmentOffset + size > segmentSize || size == 0) {
        segmentIndex = segmentIndex + 1;
        segmentOffset = 0;
        position = segmentIndex * segmentSize;
        segment = memory.segment((int) segmentIndex);
        size = dataType.size(segment, (int) segmentOffset);
      }
      position += size;
      index++;

      return dataType.read(segment, (int) segmentOffset);
    }

    public long getPosition() {
      return position;
    }
  }
}
