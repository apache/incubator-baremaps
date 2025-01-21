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

package org.apache.baremaps.shapefile;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.baremaps.store.DataRow;
import org.apache.baremaps.store.DataSchema;
import org.apache.baremaps.store.DataStoreException;
import org.apache.baremaps.store.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table that stores rows in a shapefile.
 */
public class ShapefileDataTable implements DataTable {

  private static final Logger logger = LoggerFactory.getLogger(ShapefileDataTable.class);

  private final ShapefileReader shapeFile;

  private ShapefileIterator iterator;

  /**
   * Constructs a table from a shapefile.
   *
   * @param file the path to the shapefile
   */
  public ShapefileDataTable(Path file) {
    this.shapeFile = new ShapefileReader(file.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() throws DataStoreException {
    try (var input = shapeFile.read()) {
      return input.schema();
    } catch (IOException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long size() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<DataRow> iterator() {
    try {
      return (iterator = new ShapefileIterator(shapeFile));
    } catch (IOException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws Exception {
    if (iterator != null) {
      iterator.close();
    }
  }

  /**
   * An iterator over the rows of a shapefile.
   */
  public static class ShapefileIterator implements Iterator<DataRow>, AutoCloseable {

    private final ShapefileInputStream shapefileInputStream;

    private DataRow next;

    /**
     * Constructs an iterator from a shapefile input stream.
     *
     * @param shapefileReader the shapefile input stream
     */
    public ShapefileIterator(ShapefileReader shapefileReader) throws IOException {
      this.shapefileInputStream = shapefileReader.read();
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
      } catch (IOException e) {
        logger.error("Malformed shapefile", e);
        try {
          shapefileInputStream.close();
        } catch (IOException ex) {
          // ignore
        }
        return false;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataRow next() {
      try {
        if (next == null) {
          next = shapefileInputStream.readRow();
        }
        DataRow current = next;
        next = null;
        return current;
      } catch (Exception e) {
        try {
          shapefileInputStream.close();
        } catch (IOException ex) {
          // ignore
        }
        throw new NoSuchElementException();
      }
    }

    @Override
    public void close() throws Exception {
      shapefileInputStream.close();
    }
  }
}
