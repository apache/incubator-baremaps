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

package org.apache.baremaps.storage.shapefile;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.baremaps.storage.Store;
import org.apache.baremaps.storage.Table;
import org.apache.baremaps.storage.TableException;

/**
 * A store corresponding to the shapefiles of a directory.
 */
public class ShapefileStore implements Store {

  private final Path directory;

  /**
   * Constructs a store from a directory.
   *
   * @param directory the directory
   */
  public ShapefileStore(Path directory) {
    this.directory = directory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<String> list() {
    try (var files = Files.list(directory)) {
      return files
          .filter(file -> file.toString().toLowerCase().endsWith(".shp"))
          .map(file -> file.getFileName().toString())
          .toList();
    } catch (IOException e) {
      throw new TableException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Table get(String name) {
    return new ShapefileTable(directory.resolve(name));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(Table value) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) {
    throw new UnsupportedOperationException();
  }
}
