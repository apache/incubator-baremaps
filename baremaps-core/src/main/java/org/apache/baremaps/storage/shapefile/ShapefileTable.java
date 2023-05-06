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
import org.apache.baremaps.storage.*;
import org.apache.baremaps.storage.shapefile.internal.ShapefileInputStream;
import org.apache.baremaps.storage.shapefile.internal.ShapefileReader;

/**
 * A table that stores rows in a shapefile.
 */
public class ShapefileTable extends AbstractTable {

  private final ShapefileReader shapeFile;

  /**
   * Constructs a table from a shapefile.
   *
   * @param file the path to the shapefile
   */
  public ShapefileTable(Path file) {
    this.shapeFile = new ShapefileReader(file.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Schema schema() throws TableException {
    try (var input = shapeFile.read()) {
      return input.getSchema();
    } catch (IOException e) {
      throw new TableException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Row> iterator() {
    try {
      return new ShapefileIterator(shapeFile.read());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long sizeAsLong() {
    return Long.MAX_VALUE;
  }


  /**
   * An iterator over the rows of a shapefile.
   */
  public static class ShapefileIterator implements Iterator<Row> {

    private final ShapefileInputStream shapefileInputStream;

    private Row next;

    /**
     * Constructs an iterator from a shapefile input stream.
     *
     * @param shapefileInputStream the shapefile input stream
     */
    public ShapefileIterator(ShapefileInputStream shapefileInputStream) {
      this.shapefileInputStream = shapefileInputStream;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
