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

import java.util.Collection;
import java.util.Iterator;
import org.apache.baremaps.collection.AbstractDataCollection;
import org.apache.baremaps.collection.DataCollection;

/**
 * A table is a collection of rows respecting a schema.
 */
public class TableImpl extends AbstractDataCollection<Row> implements Table {

  private final Schema schema;

  private final Collection<Row> rows;

  /**
   * Constructs a table with the specified schema.
   *
   * @param schema the schema of the table
   * @param rows the collection of rows
   */
  public TableImpl(Schema schema, Collection<Row> rows) {
    this.schema = schema;
    this.rows = rows;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Schema schema() {
    return schema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(Row e) {
    return rows.add(e);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Row> iterator() {
    return rows.iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    if (rows instanceof DataCollection dataCollection) {
      return dataCollection.sizeAsLong();
    } else {
      return rows.size();
    }
  }
}
