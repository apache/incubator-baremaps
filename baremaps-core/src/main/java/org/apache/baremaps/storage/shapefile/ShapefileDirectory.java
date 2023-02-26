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
import org.apache.baremaps.dataframe.DataFrame;
import org.apache.baremaps.dataframe.DataFrameException;
import org.apache.baremaps.dataframe.DataStore;

public class ShapefileDirectory implements DataStore {

  private final Path directory;

  public ShapefileDirectory(Path directory) {
    this.directory = directory;
  }

  @Override
  public Collection<String> list() {
    try {
      return Files.list(directory)
          .filter(file -> file.toString().toLowerCase().endsWith(".shp"))
          .map(file -> file.getFileName().toString())
          .toList();
    } catch (IOException e) {
      throw new DataFrameException(e);
    }
  }

  @Override
  public DataFrame get(String name) {
    return new ShapefileDataFrame(directory.resolve(name));
  }

  @Override
  public void add(DataFrame value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(String name) {
    throw new UnsupportedOperationException();
  }
}
