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

import org.apache.baremaps.collection.DataCollection;

/**
 * A table is a collection of rows respecting a schema.
 */
public interface Table extends DataCollection<Row> {

  /**
   * Returns the schema of the table.
   *
   * @return the schema of the table
   */
  Schema schema();

}
