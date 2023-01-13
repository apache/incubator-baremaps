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



import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A sparse map of data backed by a {@link FixedSizeDataList} for storing values.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class MonotonicSparseDataMap<T> extends DataMap<T> {

  // The key space is broken into chunks of 256 and for each chunk, store:
  // 1) the index in the outputs array for the first key in the block
  private final List<Long> offsets;
  // 2) the number of leading 0's at the start of each block
  private final ByteArrayList offsetStartPad;

  private final DataList<T> values;
  private int lastChunk = -1;
  private int lastOffset = 0;

  /**
   * Constructs a map.
   *
   * @param values the list of values
   */
  public MonotonicSparseDataMap(DataList<T> values) {
    this.offsets = new LongArrayList();
    this.offsetStartPad = new ByteArrayList();
    this.values = values;
  }

  public T get(Object keyObject) {
    long key = (long) keyObject;
    int chunk = (int) (key >>> 8);
    int offset = (int) (key & 255);
    if (chunk >= offsets.size()) {
      return null;
    }

    long lo = offsets.get(chunk);
    long hi = Math.min(
        values.sizeAsLong(),
        chunk >= offsets.size() - 1
            ? values.sizeAsLong()
            : offsets.get(chunk + 1))
        - 1;
    int startPad = offsetStartPad.getByte(chunk) & 255;

    long index = lo + offset - startPad;

    if (index > hi || index < lo) {
      return null;
    }

    return values.get(index);
  }

  public T put(Long key, T value) {
    long index = values.sizeAsLong();
    int chunk = (int) (key >>> 8);
    int offset = (int) (key & 255);
    if (chunk != lastChunk) {
      // new chunk, store offset and leading zeros
      lastOffset = offset;
      while (offsets.size() <= chunk) {
        offsets.add(index);
        offsetStartPad.add((byte) offset);
      }
      lastChunk = chunk;
    } else {
      // same chunk, write not_founds until we get to right idx
      while (++lastOffset < offset) {
        values.append(null);
      }
    }
    values.append(value);
    return null;
  }

  @Override
  protected Iterator<Long> keyIterator() {
    return new Iterator<>() {
      int currentChunk = 0;
      int currentOffset = offsetStartPad.getByte(0) & 255;

      @Override
      public boolean hasNext() {
        return currentChunk < offsets.size();
      }

      @Override
      public Long next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        long key = ((long) currentChunk << 8) + currentOffset;
        currentOffset++;
        if (currentOffset >= 255) {
          currentChunk++;
          currentOffset = offsetStartPad.getByte(currentChunk) & 255;
        }
        return key;
      }
    };
  }

  @Override
  protected Iterator<T> valueIterator() {
    return values.iterator();
  }

  @Override
  protected Iterator<Entry<Long, T>> entryIterator() {
    return Streams.zip(
        Streams.stream(keyIterator()),
        Streams.stream(valueIterator()),
        Map::entry)
        .iterator();
  }

  @Override
  public long sizeAsLong() {
    return values.sizeAsLong();
  }

  @Override
  public boolean containsKey(Object keyObject) {
    if (!(keyObject instanceof Long)) {
      return false;
    }
    long key = (long) keyObject;
    int chunk = (int) (key >>> 8);
    int offset = (int) (key & 255);
    if (chunk >= offsets.size()) {
      return false;
    }

    long lo = offsets.get(chunk);
    long hi = Math.min(
        values.sizeAsLong(),
        chunk >= offsets.size() - 1
            ? values.sizeAsLong()
            : offsets.get(chunk + 1))
        - 1;
    int startPad = offsetStartPad.getByte(chunk) & 255;

    long index = lo + offset - startPad;

    return index >= lo && index <= hi;
  }

  @Override
  public boolean containsValue(Object value) {
    return values.contains(value);
  }

  @Override
  public T remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    offsets.clear();
    offsetStartPad.clear();
    values.clear();
  }

}
