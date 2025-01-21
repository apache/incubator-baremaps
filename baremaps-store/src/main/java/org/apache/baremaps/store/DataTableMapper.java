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

package org.apache.baremaps.store;


import java.util.Iterator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A decorator for a {@link DataTable} that applies a transformation to each row.
 */
public class DataTableMapper implements DataTable {

  private final DataTable table;

  private final Function<DataRow, DataRow> transformer;

  /**
   * Constructs a new {@link DataTableMapper} with the specified table and row transformer.
   *
   * @param table the table
   * @param mapper the mapper
   */
  public DataTableMapper(DataTable table, UnaryOperator<DataRow> mapper) {
    this.table = table;
    this.transformer = mapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() {
    return table.schema();
  }

  @Override
  public long size() {
    return table.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<DataRow> iterator() {
    return table.stream().map(this.transformer).iterator();
  }

  @Override
  public void clear() {
    table.clear();
  }

  @Override
  public void close() throws Exception {
    table.close();
  }
}
