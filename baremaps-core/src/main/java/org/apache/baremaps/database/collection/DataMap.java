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

package org.apache.baremaps.database.collection;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.Map;

/**
 * A map of longs to values that can be stored in memory or on disk and has a size that can exceed
 * the maximum value of an integer.
 *
 * @param <E>
 */
public interface DataMap<E> extends Map<Long, E> {

  /**
   * Returns the size of the map as a long.
   *
   * @return the size of the map
   */
  long size64();

  /** {@inheritDoc} */
  @Override
  default int size() {
    return (int) Math.min(size64(), Integer.MAX_VALUE);
  }

  /**
   * Returns the value associated with the specified key or null if the key is not present.
   *
   * @param keys the keys
   * @return the values
   */
  default List<E> getAll(List<Long> keys) {
    return Streams.stream(keys).map(this::get).toList();
  }

}
