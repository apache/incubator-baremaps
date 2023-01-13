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
 * A dense map of data backed by a {@link FixedSizeDataType} and a {@link Memory}.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class MemoryAlignedDataMap<T> extends DataMap<T> {

  private final FixedSizeDataType<T> dataType;

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
  public MemoryAlignedDataMap(FixedSizeDataType<T> dataType, Memory memory) {
    if (dataType.size() > memory.segmentSize()) {
      throw new DataCollectionException("The values are too big");
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
  public T put(Long key, T value) {
    long position = key << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    T previous = dataType.read(segment, segmentOffset);
    dataType.write(segment, segmentOffset, value);
    return previous;
  }

  /** {@inheritDoc} */
  public T get(Object key) {
    long position = (long) key << valueShift;
    int segmentIndex = (int) (position >>> segmentShift);
    int segmentOffset = (int) (position & segmentMask);
    ByteBuffer segment = memory.segment(segmentIndex);
    return dataType.read(segment, segmentOffset);
  }

  @Override
  public T remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsKey(Object keyObject) {
    if (keyObject instanceof Long key) {
      return key >= 0 && key < size();
    } else {
      return false;
    }
  }

  @Override
  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  @Override
  public long sizeAsLong() {
    return memory.size() / dataType.size();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

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

  @Override
  protected Iterator<T> valueIterator() {
    return new Iterator<>() {

      private long index = 0;

      private long size = sizeAsLong();

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return get(index++);
      }
    };
  }

  @Override
  protected Iterator<Entry<Long, T>> entryIterator() {
    return new Iterator<>() {

      private long index = 0;

      private long size = sizeAsLong();

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public Entry<Long, T> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        long key = index++;
        return Map.entry(key, get(key));
      }
    };
  }
}
