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

package org.apache.baremaps.storage;

import java.util.Collection;

/**
 * A store is a collection of tables.
 */
public interface Store {

  /**
   * Lists the names of the tables.
   * 
   * @return the names of the tables
   */
  Collection<String> list() throws TableException;

  /**
   * Gets a table by its name.
   * 
   * @param name the name of the table
   * @return the table
   */
  Table get(String name) throws TableException;

  /**
   * Adds a table to the store.
   * 
   * @param value the table
   */
  void add(Table value) throws TableException;

  /**
   * Removes a table from the store.
   * 
   * @param name the name of the table
   */
  void remove(String name) throws TableException;

}
