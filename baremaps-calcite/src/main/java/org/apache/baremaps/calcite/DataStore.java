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

package org.apache.baremaps.calcite;

import java.util.List;

/**
 * A {@link DataStore} is a collection of {@link DataTable}s.
 */
public interface DataStore {

  /**
   * Lists the names of the data tables.
   * 
   * @return the names of the data tables
   */
  List<String> list() throws DataStoreException;

  /**
   * Gets a data table by name.
   * 
   * @param name the name of the data table
   * @return the data table
   */
  DataTable get(String name) throws DataStoreException;

  /**
   * Adds a data table to the data store.
   * 
   * @param table the data table
   */
  void add(DataTable table) throws DataStoreException;

  /**
   * Adds a data table to the data store.
   *
   * @param name the name of the data table
   * @param table the data table
   * @throws DataStoreException if the data table cannot be added
   */
  void add(String name, DataTable table) throws DataStoreException;

  /**
   * Removes a data table from the data store.
   * 
   * @param name the name of the data table
   */
  void remove(String name) throws DataStoreException;

}
