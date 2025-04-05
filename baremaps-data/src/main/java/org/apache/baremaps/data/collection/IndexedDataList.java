/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.data.collection;



import org.apache.baremaps.data.type.LongDataType;

/**
 * A data list that can hold a large number of variable size data elements. This data list is backed
 * by an index and a buffer that can be either heap, off-heap, or memory mapped.
 *
 * @param <E> The type of the elements.
 */
public class IndexedDataList<E> implements DataList<E> {

  private final DataList<Long> index;

  private final AppendOnlyLog<E> values;

  /**
   * Static factory method to create a new builder.
   *
   * @param <E> the type of elements
   * @return a new builder
   */
  public static <E> Builder<E> builder() {
    return new Builder<>();
  }

  /**
   * Builder for {@link IndexedDataList}.
   *
   * @param <E> the type of elements
   */
  public static class Builder<E> {
    private DataList<Long> index;
    private AppendOnlyLog<E> values;

    /**
     * Sets the index for the list.
     *
     * @param index the index
     * @return this builder
     */
    public Builder<E> index(DataList<Long> index) {
      this.index = index;
      return this;
    }

    /**
     * Sets the values for the list.
     *
     * @param values the values
     * @return this builder
     */
    public Builder<E> values(AppendOnlyLog<E> values) {
      this.values = values;
      return this;
    }

    /**
     * Builds a new {@link IndexedDataList}.
     *
     * @return a new IndexedDataList
     * @throws IllegalStateException if values are missing
     */
    public IndexedDataList<E> build() {
      if (values == null) {
        throw new IllegalStateException("Values must be specified");
      }

      if (index == null) {
        index = MemoryAlignedDataList.<Long>builder()
            .dataType(new LongDataType())
            .build();
      }

      return new IndexedDataList<>(index, values);
    }
  }

  /**
   * Constructs a {@link IndexedDataList}.
   *
   * @param index the index
   * @param values the values
   */
  private IndexedDataList(DataList<Long> index, AppendOnlyLog<E> values) {
    this.index = index;
    this.values = values;
  }

  /** {@inheritDoc} */
  @Override
  public long addIndexed(E value) {
    long position = values.addPositioned(value);
    return index.addIndexed(position);
  }

  /** {@inheritDoc} */
  @Override
  public void set(long index, E value) {
    long position = values.addPositioned(value);
    this.index.set(index, position);
  }

  /** {@inheritDoc} */
  @Override
  public E get(long index) {
    long position = this.index.get(index);
    return values.getPositioned(position);
  }

  /** {@inheritDoc} */
  @Override
  public long size() {
    return index.size();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    index.clear();
    values.clear();
  }

  @Override
  public void close() throws Exception {
    try {
      index.close();
      values.close();
    } catch (Exception e) {
      throw new DataCollectionException(e);
    }
  }
}
