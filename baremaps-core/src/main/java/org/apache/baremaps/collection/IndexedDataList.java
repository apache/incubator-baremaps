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



import org.apache.baremaps.collection.type.LongDataType;

/**
 * A list that can hold a large number of variable size data elements.
 *
 * This list is backed by an index and a buffer that can be either heap, off-heap, or memory mapped.
 *
 * @param <E> The type of the elements.
 */
public class IndexedDataList<E> extends DataList<E> {

  private final DataList<Long> index;

  private final AppendOnlyBuffer<E> values;


  /**
   * Constructs a list.
   *
   * @param values the values
   */
  public IndexedDataList(AppendOnlyBuffer<E> values) {
    this(new MemoryAlignedDataList<>(new LongDataType()), values);
  }

  /**
   * Constructs a list.
   *
   * @param index the index
   * @param values the values
   */
  public IndexedDataList(DataList<Long> index, AppendOnlyBuffer<E> values) {
    this.index = index;
    this.values = values;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long addIndexed(E value) {
    long position = values.addPositioned(value);
    return index.addIndexed(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void set(long index, E value) {
    long position = values.addPositioned(value);
    this.index.set(index, position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E get(long index) {
    long position = this.index.get(index);
    return values.read(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return index.sizeAsLong();
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
