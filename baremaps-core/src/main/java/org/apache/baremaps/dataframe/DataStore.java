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

package org.apache.baremaps.dataframe;

import java.util.Collection;

/**
 * A data store is a collection of data frames.
 */
public interface DataStore {

  /**
   * Lists the names of the data frames.
   * 
   * @return the names of the data frames
   */
  Collection<String> list() throws DataFrameException;

  /**
   * Gets a data frame by its name.
   * 
   * @param name the name of the data frame
   * @return the data frame
   */
  DataFrame get(String name) throws DataFrameException;

  /**
   * Adds a data frame to the data store.
   * 
   * @param value the data frame
   */
  void add(DataFrame value) throws DataFrameException;

  /**
   * Removes a data frame from the data store.
   * 
   * @param name the name of the data frame
   */
  void remove(String name) throws DataFrameException;

}
