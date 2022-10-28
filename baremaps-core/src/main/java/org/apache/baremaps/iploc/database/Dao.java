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

package org.apache.baremaps.iploc.database;



import java.util.List;
import java.util.Optional;

/**
 * A basic DAO interface to implement the data access object pattern
 *
 * @param <T> the type of entity
 */
public interface Dao<T> {

  /**
   * Find the element with the given id
   *
   * @param id the id of the element to find
   * @return the optional element
   */
  Optional<T> findOne(long id);

  /**
   * Find all the elements
   *
   * @return the list of all the elements
   */
  List<T> findAll();

  /**
   * Save the given element
   *
   * @param t the element to save
   */
  void save(T t);

  /**
   * Update the given element
   *
   * @param t the element to update
   * @param params the list of fields to update
   */
  void update(T t, String[] params);

  /**
   * Delete the given element
   *
   * @param t the element to delete
   */
  void delete(T t);
}
