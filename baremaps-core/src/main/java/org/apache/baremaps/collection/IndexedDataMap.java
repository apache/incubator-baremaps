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
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A map that can hold a large number of variable size data elements.
 *
 * This map is backed by an index and a buffer that can be either heap, off-heap, or memory mapped.
 *
 * @param <E> The type of the elements.
 */
public class IndexedDataMap<E> extends DataMap<E> {

  private final Map<Long, Long> index;

  private final AppendOnlyBuffer<E> values;

  /**
   * Constructs a map.
   *
   * @param values the values
   */
  public IndexedDataMap(AppendOnlyBuffer<E> values) {
    this(new Long2LongOpenHashMap(), values);
  }

  /**
   * Constructs a map.
   *
   * @param index the index
   * @param values the values
   */
  public IndexedDataMap(Map<Long, Long> index, AppendOnlyBuffer<E> values) {
    this.index = index;
    this.values = values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E put(Long key, E value) {
    var oldIndex = index.get(key);
    var position = values.addPositioned(value);
    index.put(key, position);
    return oldIndex == null ? null : values.read(oldIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E get(Object key) {
    var position = index.get(key);
    return position == null ? null : values.read(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<Long> keyIterator() {
    return index.keySet().iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<E> valueIterator() {
    return Streams.stream(keyIterator()).map(this::get).iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<Entry<Long, E>> entryIterator() {
    return Streams.stream(keyIterator()).map(k -> Map.entry(k, get(k))).iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return index.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    if (index instanceof DataMap<Long>dataMap) {
      return dataMap.sizeAsLong();
    } else {
      return index.size();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsKey(Object key) {
    return index.containsKey(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsValue(Object value) {
    return index.values().stream().map(values::read).anyMatch(value::equals);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E remove(Object key) {
    return values.read(index.remove(key));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    index.clear();
    values.clear();
  }
}
