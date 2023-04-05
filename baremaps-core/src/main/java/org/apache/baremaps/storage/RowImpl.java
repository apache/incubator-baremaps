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

import java.util.List;

/**
 * A row in a table.
 */
public record RowImpl(Schema schema, List values) implements Row {

  /**
   * {@inheritDoc}
   */
  @Override
  public Object get(String column) {
    for (int i = 0; i < schema().columns().size(); i++) {
      if (schema().columns().get(i).name().equals(column)) {
        return values.get(i);
      }
    }
    throw new IllegalArgumentException("Column " + column + " not found.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void set(String column, Object value) {
    for (int i = 0; i < schema().columns().size(); i++) {
      if (schema().columns().get(i).name().equals(column)) {
        values.set(i, value);
        return;
      }
    }
    throw new IllegalArgumentException("Column " + column + " not found.");
  }

}
