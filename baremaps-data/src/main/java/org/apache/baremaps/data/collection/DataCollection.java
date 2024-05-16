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

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A {@code DataCollection<E>} is a group of elements that can be iterated over. It is similar to a
 * {@link java.util.Collection<E> Collection}, but can hold up to {@link Long#MAX_VALUE} elements.
 */
public interface DataCollection<E> extends Iterable<E> {

  /**
   * Returns the number of values stored in the data collection.
   *
   * @return the number of values
   */
  long size();

  /**
   * Returns true if the data collection is empty.
   *
   * @return true if the data collection is empty
   */
  default boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns an iterator over the elements in the data collection.
   *
   * @return an iterator
   */
  @Override
  Iterator<E> iterator();

  /**
   * Returns a spliterator over the elements in the data collection.
   *
   * @return a spliterator
   */
  @Override
  default Spliterator<E> spliterator() {
    return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED);
  }

  /**
   * Returns a stream over the elements in the data collection.
   *
   * @return a stream
   */
  default Stream<E> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * Returns a parallel stream over the elements in the data collection.
   *
   * @return a parallel stream
   */
  default Stream<E> parallelStream() {
    return StreamSupport.stream(spliterator(), true);
  }

  /**
   * Adds a value to the data collection.
   *
   * @param e the value to add
   * @return true if the data collection has been modified
   */
  default boolean add(E e) {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds all the values in the specified collection to the data collection.
   *
   * @param c the collection of values to add
   * @return true if the data collection has been modified
   */
  default boolean addAll(Iterable<? extends E> c) {
    boolean modified = false;
    for (E e : c) {
      if (add(e)) {
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Returns true if the data collection contains the specified value.
   *
   * @param o the value to search for
   */
  default boolean contains(Object o) {
    for (E e : this) {
      if (e.equals(o)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the data collection contains all the values in the specified collection.
   *
   * @param c the collection of values to search for
   * @return true if the data collection contains all the values
   */
  default boolean containsAll(Iterable<?> c) {
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Removes all the values from the data collection.
   */
  void clear();

}
