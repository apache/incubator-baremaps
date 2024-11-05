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
 * A row in a table.
 */
public record DataRowImpl(DataSchema schema, List<Object> values) implements DataRow {

  /**
   * {@inheritDoc}
   */
  @Override
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
  @Override
  public Object get(int index) {
    return values.get(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
  @Override
  public void set(int index, Object value) {
    values.set(index, value);
  }
}
