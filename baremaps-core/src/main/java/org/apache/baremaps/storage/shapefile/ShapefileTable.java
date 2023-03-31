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
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.baremaps.collection.AbstractDataCollection;
import org.apache.baremaps.storage.Row;
import org.apache.baremaps.storage.Schema;
import org.apache.baremaps.storage.Table;
import org.apache.baremaps.storage.TableException;
import org.apache.baremaps.storage.shapefile.internal.ShapefileInputStream;
import org.apache.baremaps.storage.shapefile.internal.ShapefileReader;

public class ShapefileTable extends AbstractDataCollection<Row>
    implements Table, AutoCloseable {

  private final ShapefileReader shapeFile;

  public ShapefileTable(Path file) {
    this.shapeFile = new ShapefileReader(file.toString());
  }

  @Override
  public Schema schema() throws TableException {
    try (var input = shapeFile.read()) {
      return input.getSchema();
    } catch (IOException e) {
      throw new TableException(e);
    }
  }

  @Override
  public Iterator<Row> iterator() {
    try {
      return new RowIterator(shapeFile.read());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long sizeAsLong() {
    return 0;
  }

  @Override
  public void close() throws Exception {

  }

  static class RowIterator implements Iterator<Row> {

    private final ShapefileInputStream shapefileInputStream;

    private Row next;

    public RowIterator(ShapefileInputStream shapefileInputStream) {
      this.shapefileInputStream = shapefileInputStream;
    }

    @Override
    public boolean hasNext() {
      try {
        if (next == null) {
          next = shapefileInputStream.readRow();
        }
        return next != null;
      } catch (IOException exception) {
        shapefileInputStream.close();
        return false;
      }
    }

    @Override
    public Row next() {
      try {
        if (next == null) {
          next = shapefileInputStream.readRow();
        }
        Row current = next;
        next = null;
        return current;
      } catch (Exception e) {
        shapefileInputStream.close();
        throw new NoSuchElementException();
      }
    }
  }
}
