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

package org.apache.baremaps.database.schema;

import java.util.Iterator;
import org.apache.baremaps.database.collection.DataCollection;

/**
 * A table is a collection of rows respecting a row type.
 */
public class DataTableImpl implements DataTable {

  private final DataRowType rowType;

  private final DataCollection<DataRow> rows;

  /**
   * Constructs a table with the specified row type.
   *
   * @param rowType the row type of the table
   * @param rows the collection of rows
   */
  public DataTableImpl(DataRowType rowType, DataCollection<DataRow> rows) {
    this.rowType = rowType;
    this.rows = rows;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataRowType rowType() {
    return rowType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(DataRow e) {
    return rows.add(e);
  }

  @Override
  public void clear() {
    rows.clear();
  }

  @Override
  public long size() {
    return rows.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<DataRow> iterator() {
    return rows.iterator();
  }

}
