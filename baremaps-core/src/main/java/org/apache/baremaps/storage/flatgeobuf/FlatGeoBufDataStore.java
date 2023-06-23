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

package org.apache.baremaps.storage.flatgeobuf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.baremaps.database.table.DataStore;
import org.apache.baremaps.database.table.DataTable;
import org.apache.baremaps.database.table.DataTableException;

/**
 * A store corresponding to the flatgeobuf files of a directory.
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
  public Collection<String> list() throws DataTableException {
    try (var files = Files.list(directory)) {
      return files
          .filter(file -> file.toString().toLowerCase().endsWith(".fgb"))
          .map(file -> file.getFileName().toString())
          .toList();
    } catch (IOException e) {
      throw new DataTableException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataTable get(String name) throws DataTableException {
    var path = directory.resolve(name);
    return new FlatGeoBufDataTable(path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(DataTable table) throws DataTableException {
    var filename = table.schema().name();
    filename = filename.endsWith(".fgb") ? filename : filename + ".fgb";
    var path = directory.resolve(filename);
    try {
      Files.deleteIfExists(path);
      Files.createFile(path);
      var flatGeoBufTable = new FlatGeoBufDataTable(path, table.schema());
      flatGeoBufTable.write(table);
    } catch (IOException e) {
      throw new DataTableException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) throws DataTableException {
    var path = directory.resolve(name);
    if (name.equals(path.getFileName().toString())) {
      try {
        Files.delete(path);
      } catch (IOException e) {
        throw new DataTableException(e);
      }
    } else {
      throw new DataTableException("Table not found");
    }
  }
}
