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

package org.apache.baremaps.data.schema;

import java.util.List;

/**
 * A {@link DataSchema} is a collection of {@link DataFrame}s.
 */
public interface DataStore {

  /**
   * Lists the names of the data frames.
   * 
   * @return the names of the data frames
   */
  List<String> list() throws DataStoreException;

  /**
   * Gets a data frame by name.
   * 
   * @param name the name of the data frame
   * @return the data frame
   */
  DataFrame get(String name) throws DataStoreException;

  /**
   * Adds a data frame to the data store.
   * 
   * @param frame the data frame
   */
  void add(DataFrame frame) throws DataStoreException;

  /**
   * Adds a data frame to the data store.
   *
   * @param name the name of the data frame
   * @param frame the data frame
   * @throws DataStoreException if the data frame cannot be added
   */
  void add(String name, DataFrame frame) throws DataStoreException;

  /**
   * Removes a data frame from the data store.
   * 
   * @param name the name of the data frame
   */
  void remove(String name) throws DataStoreException;

}
