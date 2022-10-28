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



import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.List;

/**
 * A sparse map of data backed by a {@link AlignedDataList} for storing values.
 *
 * <p>
 * This code has been adapted from Planetiler (Apache license).
 *
 * <p>
 * Copyright (c) Planetiler.
 */
public class LongSizedDataSparseMap<T> implements LongDataMap<T> {

  // The key space is broken into chunks of 256 and for each chunk, store:
  // 1) the index in the outputs array for the first key in the block
  private final List<Long> offsets;
  // 2) the number of leading 0's at the start of each block
  private final ByteArrayList offsetStartPad;

  private final AlignedDataList<T> values;
  private int lastChunk = -1;
  private int lastOffset = 0;

  /**
   * Constructs a map.
   *
   * @param values the list of values
   */
  public LongSizedDataSparseMap(AlignedDataList<T> values) {
    this.offsets = new LongArrayList();
    this.offsetStartPad = new ByteArrayList();
    this.values = values;
  }

  @Override
  public void put(long key, T value) {
    long index = values.size();
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
        values.add(null);
      }
    }
    values.add(value);
  }

  @Override
  public T get(long key) {
    int chunk = (int) (key >>> 8);
    int offset = (int) (key & 255);
    if (chunk >= offsets.size()) {
      return null;
    }

    long lo = offsets.get(chunk);
    long hi = Math.min(values.size(),
        chunk >= offsets.size() - 1 ? values.size() : offsets.get(chunk + 1)) - 1;
    int startPad = offsetStartPad.getByte(chunk) & 255;

    long index = lo + offset - startPad;

    if (index > hi || index < lo) {
      return null;
    }

    return values.get(index);
  }
}
