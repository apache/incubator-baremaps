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
 * A row in a {@link DataTable}.
 */
public record DataRow(DataSchema schema, List<Object> values) {

  /**
   * {@inheritDoc}
   */
  public Object get(String column) {
    var columns = schema.columns();
    for (int i = 0; i < columns.size(); i++) {
      if (columns.get(i).name().equals(column)) {
        return values.get(i);
      }
    }
    throw new IllegalArgumentException("Column " + column + " not found.");
  }

  /**
   * {@inheritDoc}
   */
  public Object get(int index) {
    return values.get(index);
  }

  /**
   * {@inheritDoc}
   */
  public void set(String column, Object value) {
    for (int i = 0; i < schema.columns().size(); i++) {
      if (schema.columns().get(i).name().equals(column)) {
        values.set(i, value);
        return;
      }
    }
    throw new IllegalArgumentException("Column " + column + " not found.");
  }

  /**
   * {@inheritDoc}
   */
  public void set(int index, Object value) {
    values.set(index, value);
  }

  /**
   * Sets the value of the specified column and returns the row.
   *
   * @param column the column
   * @param value the value
   * @return the row
   */
  public DataRow with(String column, Object value) {
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
  public DataRow with(int index, Object value) {
    set(index, value);
    return this;
  }
}
