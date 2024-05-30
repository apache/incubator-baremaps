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

package org.apache.baremaps.data.storage;

import java.util.List;

/**
 * A row in a {@link DataTable}.
 */
public interface DataRow {

  /**
   * Returns the schema of the row.
   * 
   * @return the schema of the row
   */
  DataSchema schema();

  /**
   * Returns the values of the columns in the row.
   * 
   * @return the values of the columns in the row
   */
  List<?> values();

  /**
   * Returns the value of the specified column.
   * 
   * @param column the column
   * @return the value of the specified column
   */
  Object get(String column);

  /**
   * Returns the value of the specified column.
   *
   * @param index the index of the column
   * @return the value of the specified column
   */
  Object get(int index);

  /**
   * Sets the value of the specified column.
   * 
   * @param column the column
   * @param value the value
   */
  void set(String column, Object value);

  /**
   * Sets the value of the specified column.
   *
   * @param index the index of the column
   * @param value the value
   */
  void set(int index, Object value);

  /**
   * Sets the value of the specified column and returns the row.
   *
   * @param column the column
   * @param value the value
   * @return the row
   */
  default DataRow with(String column, Object value) {
    set(column, value);
    return this;
  }

  /**
   * Sets the value of the specified column and returns the row.
   *
   * @param index the index of the column
   * @param value the value
   * @return the row
   */
  default DataRow with(int index, Object value) {
    set(index, value);
    return this;
  }
}
