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

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.apache.baremaps.storage.Store;
import org.apache.baremaps.storage.Table;
import org.apache.baremaps.storage.TableException;

public class FlatGeoBufStore implements Store {

  private final Path file;

  public FlatGeoBufStore(Path file) {
    this.file = file;
  }

  @Override
  public Collection<String> list() throws TableException {
    return List.of(file.getFileName().toString());
  }

  @Override
  public Table get(String name) throws TableException {
    if (name.equals(file.getFileName().toString())) {
      return new FlatGeoBufTable(file);
    } else {
      throw new TableException("Table not found");
    }
  }

  @Override
  public void add(Table value) throws TableException {

  }

  @Override
  public void remove(String name) throws TableException {

  }
}
