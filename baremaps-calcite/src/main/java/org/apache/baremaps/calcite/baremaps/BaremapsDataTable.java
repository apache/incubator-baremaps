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

package org.apache.baremaps.calcite.baremaps;

import java.io.IOException;
import java.util.Iterator;
import org.apache.baremaps.calcite.DataRow;
import org.apache.baremaps.calcite.DataRowType;
import org.apache.baremaps.calcite.DataSchema;
import org.apache.baremaps.calcite.DataTable;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataCollection;

/**
 * A {@link DataTable} is a collection of rows respecting a {@link DataSchema}.
 */
public class BaremapsDataTable implements DataTable {

  private final DataSchema schema;

  private final DataCollection<DataRow> rows;

  /**
   * Constructs a {@link DataTable} with the specified row {@link DataSchema}.
   *
   * @param schema the schema of the rows
   * @param rows the rows
   */
  public BaremapsDataTable(DataSchema schema, DataCollection<DataRow> rows) {
    this.schema = schema;
    this.rows = rows;
  }

  /**
   * Constructs a {@link DataTable} with the specified row {@link DataSchema}.
   *
   * @param schema the schema of the rows
   */
  public BaremapsDataTable(DataSchema schema) {
    this.schema = schema;
    this.rows = new AppendOnlyLog<>(new DataRowType(schema));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() {
    return schema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(DataRow row) {
    return rows.add(row);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    rows.clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long size() {
    return rows.size();
  }

  @Override
  public Iterator<DataRow> iterator() {
    return rows.iterator();
  }

  @Override
  public void close() throws IOException {
    rows.close();
  }
}
