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

package org.apache.baremaps.storage.geoparquet;


import java.net.URI;
import java.util.List;
import org.apache.baremaps.data.storage.DataStore;
import org.apache.baremaps.data.storage.DataStoreException;
import org.apache.baremaps.data.storage.DataTable;

/**
 * A {@link DataStore} corresponding to a GeoParquet file.
 */
public class GeoParquetDataStore implements DataStore {

  private final URI uri;
  private final String tableName;

  public GeoParquetDataStore(URI uri, String tableName) {
    this.uri = uri;
    this.tableName = tableName;
  }

  @Override
  public List<String> list() throws DataStoreException {
    return List.of(tableName);
  }

  @Override
  public DataTable get(String name) throws DataStoreException {
    if (!tableName.equals(name)) {
      throw new DataStoreException("Table not found");
    }
    return new GeoParquetDataTable(uri, tableName);
  }

  @Override
  public void add(DataTable table) throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(String name, DataTable table) throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(String name) throws DataStoreException {
    throw new UnsupportedOperationException();
  }
}
