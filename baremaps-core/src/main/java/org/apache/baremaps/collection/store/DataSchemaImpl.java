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

package org.apache.baremaps.collection.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A schema defines the structure of a table.
 */
public class DataSchemaImpl implements DataSchema {

  private final String name;

  private final List<DataColumn> dataColumns;

  private final Map<String, Integer> index;

  /**
   * Constructs a schema.
   *
   * @param name the name of the schema
   * @param dataColumns the columns of the schema
   */
  public DataSchemaImpl(String name, List<DataColumn> dataColumns) {
    this.name = name;
    this.dataColumns = dataColumns;
    this.index = new HashMap<>();
    for (int i = 0; i < dataColumns.size(); i++) {
      index.put(dataColumns.get(i).name(), i);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<DataColumn> columns() {
    return dataColumns;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataRow createRow() {
    var values = new ArrayList<>(dataColumns.size());
    for (int i = 0; i < dataColumns.size(); i++) {
      values.add(null);
    }
    return new DataRowImpl(this, values);
  }
}
