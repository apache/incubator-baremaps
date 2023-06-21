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
public class DataTableImpl extends AbstractDataCollection<DataRow> implements DataTable {

  private final DataSchema dataSchema;

  private final Collection<DataRow> dataRows;

  /**
   * Constructs a table with the specified schema.
   *
   * @param dataSchema the schema of the table
   * @param dataRows the collection of rows
   */
  public DataTableImpl(DataSchema dataSchema, Collection<DataRow> dataRows) {
    this.dataSchema = dataSchema;
    this.dataRows = dataRows;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() {
    return dataSchema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(DataRow e) {
    return dataRows.add(e);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<DataRow> iterator() {
    return dataRows.iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    if (dataRows instanceof DataCollection dataCollection) {
      return dataCollection.sizeAsLong();
    } else {
      return dataRows.size();
    }
  }
}
