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



import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.baremaps.collection.memory.Memory;
import org.apache.baremaps.collection.type.FixedSizeDataType;

/**
 * A map that can hold a large number of fixed-size memory-aligned data elements.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 */
public class MemoryAlignedDataMap<E> extends DataMap<E> {

  private final FixedSizeDataType<E> dataType;

  private final Memory memory;

  private final int valueShift;

  private final long segmentShift;

  private final long segmentMask;

  /**
   * Constructs a map.
   *
   * @param dataType the data type
   * @param memory the memory
   */
  public MemoryAlignedDataMap(FixedSizeDataType<E> dataType, Memory memory) {
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
  }

  /** {@inheritDoc} */
  @Override
  public E put(Long key, E value) {
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
    long position = (long) key << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }

  /** {@inheritDoc} */
  @Override
  public E remove(Object key) {
    throw new UnsupportedOperationException();
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
    return values().contains(value);
  }

  /** {@inheritDoc} */
  @Override
  public long sizeAsLong() {
    return memory.size() / dataType.size();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  protected Iterator<Long> keyIterator() {
    return new Iterator<>() {

      private long index = 0;

      private long size = sizeAsLong();

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
  protected Iterator<E> valueIterator() {
    return new Iterator<>() {

      private long index = 0;

      private long size = sizeAsLong();

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

  /** {@inheritDoc} */
  @Override
  protected Iterator<Entry<Long, E>> entryIterator() {
    return new Iterator<>() {

      private long index = 0;

      private long size = sizeAsLong();

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
