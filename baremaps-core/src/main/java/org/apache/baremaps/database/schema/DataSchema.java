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

package org.apache.baremaps.database.schema;

import java.util.Collection;

/**
 * A schema is a collection of tables.
 */
public interface DataSchema {

  /**
   * Lists the names of the tables.
   * 
   * @return the names of the tables
   */
  Collection<String> list() throws DataTableException;

  /**
   * Gets a table by its name.
   * 
   * @param name the name of the table
   * @return the table
   */
  DataTable get(String name) throws DataTableException;

  /**
   * Adds a table to the schema.
   * 
   * @param value the table
   */
  void add(DataTable value) throws DataTableException;

  /**
   * Adds a table to the schema.
   *
   * @param name the name of the table
   * @param value the table
   */
  void add(String name, DataTable value) throws DataTableException;

  /**
   * Removes a table from the schema.
   * 
   * @param name the name of the table
   */
  void remove(String name) throws DataTableException;

}
