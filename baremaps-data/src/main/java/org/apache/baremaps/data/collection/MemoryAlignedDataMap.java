/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.data.collection;



import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.type.FixedSizeDataType;

/**
 * A {@link DataMap} that can hold a large number of fixed-size memory-aligned data elements.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 */
public class MemoryAlignedDataMap<E> implements DataMap<Long, E> {

  private final FixedSizeDataType<E> dataType;

  private final Memory<?> memory;

  private final int valueShift;

  private final long segmentShift;

  private final long segmentMask;

  private final long upperBoundary;

  /**
   * Constructs a {@link MemoryAlignedDataMap}.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public MemoryAlignedDataMap(FixedSizeDataType<E> dataType, Memory<?> memory) {
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
    this.upperBoundary = segmentShift > 32 ? Long.MAX_VALUE >> valueShift
        : Long.MAX_VALUE >> (32 - segmentShift + valueShift);
  }

  private void checkBoundary(Long key) {
    Objects.requireNonNull(key, "Key couldn't be null");
    if (key < 0 || key > upperBoundary) {
      String msg =
          String.format("Key should between 0 and %d, but your key is %d", upperBoundary, key);
      throw new IndexOutOfBoundsException(msg);
    }
  }

  /** {@inheritDoc} */
  @Override
  public E put(Long key, E value) {
    checkBoundary(key);
    Objects.requireNonNull(value, "Value couldn't be null");
    long position = key << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    E previous = dataType.read(segment, segmentOffset);
    dataType.write(segment, segmentOffset, value);
    return previous;
  }

  /** {@inheritDoc} */
  @Override
  public E get(Object key) {
    checkBoundary((long) key);
    long position = (long) key << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(Object keyObject) {
    if (keyObject instanceof Long key) {
      return key >= 0 && key < size();
    } else {
      return false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsValue(Object value) {
    Iterator<E> iterator = valueIterator();
    while (iterator.hasNext()) {
      if (iterator.next().equals(value)) {
        return true;
      }
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public long size() {
    return memory.size() / dataType.size();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Long> keyIterator() {
    return new Iterator<>() {

      private final long size = size();

      private long index = 0;

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public Long next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return index++;
      }
    };
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<E> valueIterator() {
    return new Iterator<>() {

      private final long size = size();

      private long index = 0;

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return get(index++);
      }
    };
  }

  @Override
  public Iterator<Entry<Long, E>> entryIterator() {
    return new Iterator<>() {

      private final long size = size();

      private long index = 0;

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public Entry<Long, E> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        long key = index++;
        return Map.entry(key, get(key));
      }
    };
  }

}
