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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A schema defines the structure of a table.
 */
public class SchemaImpl implements Schema {

  private final String name;

  private final List<Column> columns;

  private final Map<String, Integer> index;

  public SchemaImpl(String name, List<Column> columns) {
    this.name = name;
    this.columns = columns;
    this.index = new HashMap<>();
    for (int i = 0; i < columns.size(); i++) {
      index.put(columns.get(i).name(), i);
    }
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public List<Column> columns() {
    return columns;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Row createRow() {
    var values = new ArrayList<>(columns.size());
    for (int i = 0; i < columns.size(); i++) {
      values.add(null);
    }
    return new RowImpl(this, values);
  }
}
