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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.baremaps.calcite.DataStore;
import org.apache.baremaps.calcite.DataStoreException;
import org.apache.baremaps.calcite.DataTable;

public class BaremapsDataStore implements DataStore {

  private Map<String, DataTable> tables;

  public BaremapsDataStore(List<DataTable> tables) {
    this.tables =
        tables.stream()
            .collect(Collectors.toMap(table -> table.schema().name(), table -> table));
  }

  @Override
  public List<String> list() throws DataStoreException {
    return new ArrayList<>(tables.keySet());
  }

  @Override
  public DataTable get(String name) throws DataStoreException {
    return tables.get(name);
  }

  @Override
  public void add(DataTable table) throws DataStoreException {
    tables.put(table.schema().name(), table);
  }

  @Override
  public void add(String name, DataTable table) throws DataStoreException {
    tables.put(name, table);
  }

  @Override
  public void remove(String name) throws DataStoreException {
    tables.remove(name);
  }
}
