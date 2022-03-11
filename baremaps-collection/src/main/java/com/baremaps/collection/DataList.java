/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.collection;

/**
 * A list of data.
 *
 * @param <T>
 */
public interface DataList<T> {

  /**
   * Adds a value to the list and returns its index.
   *
   * @param value the value
   * @return the index of the value
   */
  long add(T value);

  /**
   * Returns a values by its index.
   *
   * @param index the index of the value
   * @return the value
   */
  T get(long index);

  /**
   * Returns the size of the list.
   *
   * @return the size of the list.
   */
  long size();
}
