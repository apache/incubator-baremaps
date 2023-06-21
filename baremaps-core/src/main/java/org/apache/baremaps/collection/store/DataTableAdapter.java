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


import java.util.Iterator;
import java.util.function.Function;
import org.apache.baremaps.collection.AbstractDataCollection;

/**
 * A decorator for a table that transforms the geometries of the rows.
 */
public class DataTableAdapter extends AbstractDataCollection<DataRow> implements DataTable {

  private final DataTable dataTable;

  private final Function<DataRow, DataRow> transformer;

  /**
   * Constructs a new table decorator.
   *
   * @param dataTable the table to decorate
   * @param transformer the row transformer
   */
  public DataTableAdapter(DataTable dataTable, Function<DataRow, DataRow> transformer) {
    this.dataTable = dataTable;
    this.transformer = transformer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() {
    return dataTable.schema();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator iterator() {
    return dataTable.stream().map(this.transformer).iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return dataTable.sizeAsLong();
  }


}
