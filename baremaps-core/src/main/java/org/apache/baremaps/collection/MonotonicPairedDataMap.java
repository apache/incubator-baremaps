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



import java.util.Iterator;
import java.util.Map;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.PairDataType.Pair;

/**
 * A map that can hold a large number of variable-size data elements. The elements must be sorted by
 * their key and inserted in a monotonic way. The elements cannot be removed or updated once
 * inserted.
 */
public class MonotonicPairedDataMap<E> extends DataMap<E> {

  private final DataList<Long> offsets;
  private final MemoryAlignedDataList<Pair<Long, E>> values;

  private long lastChunk = -1;

  public MonotonicPairedDataMap(MemoryAlignedDataList<Pair<Long, E>> values) {
    this(new MemoryAlignedDataList<>(new LongDataType()), values);
  }

  /**
   * Constructs a map.
   *
   * @param offsets the list of offsets
   * @param values the buffer of values
   */
  public MonotonicPairedDataMap(DataList<Long> offsets,
      MemoryAlignedDataList<Pair<Long, E>> values) {
    this.offsets = offsets;
    this.values = values;
  }

  /** {@inheritDoc} */
  public E put(Long key, E value) {
    long index = values.sizeAsLong();
    long chunk = key >>> 8;
    if (chunk != lastChunk) {
      while (offsets.sizeAsLong() <= chunk) {
        offsets.add(index);
      }
      lastChunk = chunk;
    }
    values.add(new Pair<>(key, value));
    return null;
  }

  /** {@inheritDoc} */
  public E get(Object keyObject) {
    long key = (long) keyObject;
    long chunk = key >>> 8;
    if (chunk >= offsets.sizeAsLong()) {
      return null;
    }
    long lo = offsets.get(chunk);
    long hi =
        Math.min(
            values.sizeAsLong(),
            chunk >= offsets.sizeAsLong() - 1
                ? values.sizeAsLong()
                : offsets.get(chunk + 1))
            - 1;
    while (lo <= hi) {
      long index = (lo + hi) >>> 1;
      Pair<Long, E> pair = values.get(index);
      long value = pair.left();
      if (value < key) {
        lo = index + 1;
      } else if (value > key) {
        hi = index - 1;
      } else {
        // found
        return pair.right();
      }
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  protected Iterator<Long> keyIterator() {
    return values.stream().map(Pair::left).iterator();
  }

  /** {@inheritDoc} */
  @Override
  protected Iterator<E> valueIterator() {
    return values.stream().map(Pair::right).iterator();
  }

  /** {@inheritDoc} */
  @Override
  protected Iterator<Entry<Long, E>> entryIterator() {
    return values.stream()
        .map(p -> Map.entry(p.left(), p.right()))
        .iterator();
  }

  /** {@inheritDoc} */
  @Override
  public long sizeAsLong() {
    return values.sizeAsLong();
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsValue(Object value) {
    return values.stream().anyMatch(p -> p.right().equals(value));
  }

  /** {@inheritDoc} */
  @Override
  public E remove(Object key) {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    offsets.clear();
    values.clear();
  }
}
