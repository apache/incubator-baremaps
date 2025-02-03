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

package org.apache.baremaps.calcite.flatgeobuf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.baremaps.calcite.DataStore;
import org.apache.baremaps.calcite.DataStoreException;
import org.apache.baremaps.calcite.DataTable;

/**
 * A {@link DataStore} corresponding to the flatgeobuf files of a directory.
 */
public class FlatGeoBufDataStore implements DataStore {

  private final Path directory;

  public FlatGeoBufDataStore(Path directory) {
    this.directory = directory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> list() throws DataStoreException {
    try (var files = Files.list(directory)) {
      return files
          .filter(file -> file.toString().toLowerCase().endsWith(".fgb"))
          .map(file -> file.getFileName().toString())
          .toList();
    } catch (IOException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataTable get(String name) throws DataStoreException {
    var path = directory.resolve(name);
    return new FlatGeoBufDataTable(path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(DataTable table) throws DataStoreException {
    var fileName = table.schema().name();
    fileName = fileName.endsWith(".fgb") ? fileName : fileName + ".fgb";
    add(fileName, table);
  }

  @Override
  public void add(String name, DataTable table) throws DataStoreException {
    var path = directory.resolve(name);
    try {
      Files.deleteIfExists(path);
      Files.createFile(path);
      var flatGeoBufTable = new FlatGeoBufDataTable(path, table.schema());
      flatGeoBufTable.write(table);
    } catch (IOException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) throws DataStoreException {
    var path = directory.resolve(name);
    if (name.equals(path.getFileName().toString())) {
      try {
        Files.delete(path);
      } catch (IOException e) {
        throw new DataStoreException(e);
      }
    } else {
      throw new DataStoreException("Table not found");
    }
  }
}
