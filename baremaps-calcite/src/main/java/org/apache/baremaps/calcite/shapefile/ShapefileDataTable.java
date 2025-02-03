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

package org.apache.baremaps.calcite.shapefile;


import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.apache.baremaps.calcite.*;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.shapefile.ShapefileInputStream;
import org.apache.baremaps.shapefile.ShapefileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table that stores rows in a shapefile.
 */
public class ShapefileDataTable implements DataTable {

  private static final Logger logger = LoggerFactory.getLogger(ShapefileDataTable.class);

  private final ShapefileReader shapeFile;

  private final DataSchema schema;

  private DataSchema getSchema(final String name) {
    Objects.requireNonNull(name, "The row name cannot be null.");

    var columns = new ArrayList<DataColumn>();
    for (int i = 0; i < shapeFile.getDatabaseFieldsDescriptors().size(); i++) {
      var fieldDescriptor = shapeFile.getDatabaseFieldsDescriptors().get(i);
      var columnName = fieldDescriptor.getName();
      var columnType = switch (fieldDescriptor.getType()) {
        case CHARACTER -> Type.STRING;
        case NUMBER -> fieldDescriptor.getDecimalCount() == 0 ? Type.LONG : Type.DOUBLE;
        case CURRENCY -> Type.DOUBLE;
        case DOUBLE -> Type.DOUBLE;
        case INTEGER -> Type.INTEGER;
        case AUTO_INCREMENT -> Type.INTEGER;

        // TODO: Implement the following types
        case LOGICAL -> Type.STRING;
        case DATE -> Type.STRING;
        case MEMO -> Type.STRING;
        case FLOATING_POINT -> Type.STRING;
        case PICTURE -> Type.STRING;
        case VARI_FIELD -> Type.STRING;
        case VARIANT -> Type.STRING;
        case TIMESTAMP -> Type.STRING;
        case DATE_TIME -> Type.STRING;
      };
      columns.add(new DataColumnFixed(columnName, Cardinality.OPTIONAL, columnType));
    }

    // Add geometry column.
    columns.add(new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY));

    return new DataSchema(name, columns);
  }

  private ShapefileIterator iterator;

  /**
   * Constructs a table from a shapefile.
   *
   * @param file the path to the shapefile
   */
  public ShapefileDataTable(Path file) {
    this.shapeFile = new ShapefileReader(file.toString());
    this.schema = getSchema(file.getFileName().toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() throws DataStoreException {
    return schema;
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
  public class ShapefileIterator implements Iterator<DataRow>, AutoCloseable {

    private final ShapefileInputStream shapefileInputStream;

    private List<Object> next;

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
        List<Object> current = next;
        next = null;
        return new DataRow(schema, current);
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
