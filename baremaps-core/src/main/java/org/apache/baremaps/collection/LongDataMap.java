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



import java.util.List;

/**
 * A map of data.
 *
 * @param <T>
 */
public interface LongDataMap<T> {

  /**
   * Associates the specified value with the specified key in this map.
   *
   * @param key the key with which the specified value is to be associated
   * @param value the value to be associated with the specified key
   */
  void put(long key, T value);

  /**
   * Returns the value to which the specified key is mapped, or null if this map contains no mapping
   * for the key.
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped, or null if this map contains no mapping
   *         for the key
   */
  T get(long key);

  /**
   * Returns the list of values to which the specified keys are mapped, or null if this map contains
   * no mapping for the key.
   *
   * @param keys the keys whose associated value are to be returned
   * @return the values to which the specified keys are mapped
   */
  default List<T> get(List<Long> keys) {
    return keys.stream().map(this::get).toList();
  }
}
