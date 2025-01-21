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

package org.apache.baremaps.csv;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.baremaps.store.DataSchema;
import org.apache.baremaps.store.DataStore;
import org.apache.baremaps.store.DataStoreException;
import org.apache.baremaps.store.DataTable;

/**
 * A DataStore implementation that manages a single CSV file.
 */
public class CsvDataStore implements DataStore {

  private final String tableName;
  private final DataSchema schema;
  private final CsvDataTable dataTable;

  /**
   * Constructs a CsvDataStore with the specified table name, schema, and CSV file.
   *
   * @param tableName the name of the table
   * @param schema the data schema defining the structure
   * @param csvFile the CSV file to read data from
   * @param hasHeader whether the CSV file includes a header row
   * @param separator the character used to separate columns in the CSV file
   * @throws IOException if an I/O error occurs
   */
  public CsvDataStore(String tableName, DataSchema schema, File csvFile, boolean hasHeader,
      char separator) throws IOException {
    this.tableName = tableName;
    this.schema = schema;
    this.dataTable = new CsvDataTable(csvFile, hasHeader);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> list() throws DataStoreException {
    return Collections.singletonList(tableName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataTable get(String name) throws DataStoreException {
    if (this.tableName.equals(name)) {
      return dataTable;
    } else {
      throw new DataStoreException("Table '" + name + "' not found.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(DataTable table) throws DataStoreException {
    throw new UnsupportedOperationException("Adding tables is not supported in CsvDataStore.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(String name, DataTable table) throws DataStoreException {
    throw new UnsupportedOperationException("Adding tables is not supported in CsvDataStore.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) throws DataStoreException {
    throw new UnsupportedOperationException("Removing tables is not supported in CsvDataStore.");
  }
}
