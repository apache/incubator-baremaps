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

package com.baremaps.osm.database;

import com.baremaps.osm.domain.Entity;
import java.util.List;

public interface EntityTable<T extends Entity> {

  /**
   * Select an element by id
   *
   * @param id the id of the element
   * @return the selected element if it exists. Null otherwise
   * @throws DatabaseException
   */
  T select(Long id) throws DatabaseException;

  /**
   * Select a list of elements by their id
   *
   * @param ids a list of id
   * @return a list of the selected elements
   * @throws DatabaseException
   */
  List<T> select(List<Long> ids) throws DatabaseException;

  /**
   * Insert a new entity into the database
   *
   * @param entity the entity to insert
   * @throws DatabaseException
   */
  void insert(T entity) throws DatabaseException;

  /**
   * Insert a list of entities into the database
   *
   * @param entities a list of the entities to insert
   * @throws DatabaseException
   */
  void insert(List<T> entities) throws DatabaseException;

  /**
   * Delete an entity with an id
   *
   * @param id the id of the item to delete
   * @throws DatabaseException
   */
  void delete(Long id) throws DatabaseException;

  /**
   * Delete a list of entity in the database
   *
   * @param ids the list of entity
   * @throws DatabaseException
   */
  void delete(List<Long> ids) throws DatabaseException;

  /**
   * Add the given entities to the database using the copy interface
   *
   * @param entities a list of the entities to add
   * @throws DatabaseException If an exception occurs while copying
   */
  void copy(List<T> entities) throws DatabaseException;
}
