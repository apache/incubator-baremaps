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



import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@code DataList<E>} is a sequence of elements that can be iterated over and accessed by index.
 * It is similar to a {@link java.util.List<E> List}, but can hold up to {@link Long#MAX_VALUE}
 * elements.
 *
 * @param <E> The type of the elements.
 */
public interface DataList<E> extends DataCollection<E> {

  /**
   * Adds a value to the data list and returns its index.
   *
   * @param value the value
   * @return the index of the value.
   */
  long addIndexed(E value);

  /** {@inheritDoc} */
  @Override
  default boolean add(E value) {
    addIndexed(value);
    return true;
  }

  /**
   * Sets the value at the specified index
   *
   * @param index the index
   * @param value the value
   */
  void set(long index, E value);

  /**
   * Returns the value at the specified index.
   *
   * @param index the index
   * @return the value
   */
  E get(long index);

  /** {@inheritDoc} */
  @Override
  default Iterator<E> iterator() {
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
}
