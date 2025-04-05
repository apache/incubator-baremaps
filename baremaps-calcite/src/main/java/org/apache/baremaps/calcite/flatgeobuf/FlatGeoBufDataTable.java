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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.apache.baremaps.calcite.*;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.flatgeobuf.FlatGeoBuf;
import org.apache.baremaps.flatgeobuf.FlatGeoBufReader;
import org.apache.baremaps.flatgeobuf.FlatGeoBufWriter;
import org.apache.baremaps.flatgeobuf.PackedRTree;

/**
 * A {@link DataTable} that stores rows in a flatgeobuf file.
 */
public class FlatGeoBufDataTable implements DataTable {

  private final Path file;

  private final DataSchema schema;

  private RowIterator iterator;

  /**
   * Constructs a table from a flatgeobuf file (used for reading).
   *
   * @param file the path to the flatgeobuf file
   */
  public FlatGeoBufDataTable(Path file) {
    this(file, readSchema(file));
  }

  /**
   * Constructs a table from a flatgeobuf file and a schema (used for writing).
   *
   * @param file the path to the flatgeobuf file
   * @param schema the schema of the table
   */
  public FlatGeoBufDataTable(Path file, DataSchema schema) {
    this.file = file;
    this.schema = schema;
  }

  /**
   * Reads the schema from a flatgeobuf file.
   *
   * @param file the path to the flatgeobuf file
   * @return the schema of the table
   */
  private static DataSchema readSchema(Path file) {
    try (var reader = new FlatGeoBufReader(FileChannel.open(file, StandardOpenOption.READ))) {
      // try to read the schema from the file
      var header = reader.readHeader();
      return FlatGeoBufTypeConversion.asSchema(header);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataSchema schema() {
    return schema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<DataRow> iterator() {
    try {
      return (iterator = new RowIterator(file, schema));
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

  /**
   * {@inheritDoc}
   */
  @Override
  public long size() {
    try (var reader = new FlatGeoBufReader(FileChannel.open(file, StandardOpenOption.READ))) {
      FlatGeoBuf.Header header = reader.readHeader();
      return header.featuresCount();
    } catch (IOException e) {
      throw new DataStoreException(e);
    }
  }

  /**
   * Writes a table to a flatgeobuf file.
   *
   * @param table the table to write
   * @throws IOException if an error occurs while writing the rows
   */
  public void write(DataTable table) throws IOException {
    try (
        var writer = new FlatGeoBufWriter(
            FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {

      schema.columns().stream()
          .filter(c -> c.cardinality() == DataColumn.Cardinality.REQUIRED)
          .forEach(c -> {
            if (Objects.requireNonNull(c.type()) == Type.BINARY) {
              throw new UnsupportedOperationException();
            }
          });

      var header = new FlatGeoBuf.Header(
          schema.name(),
          null,
          FlatGeoBuf.GeometryType.UNKNOWN,
          false,
          false,
          false,
          false,
          FlatGeoBufTypeConversion.asColumns(schema.columns()),
          table.size(),
          2,
          null,
          null,
          null,
          null);

      writer.writeHeader(header);

      var indexSize =
          (int) PackedRTree.calcSize((int) header.featuresCount(), header.indexNodeSize());

      writer.writeIndexBuffer(ByteBuffer.allocate(indexSize).order(ByteOrder.LITTLE_ENDIAN));

      var iterator = table.iterator();
      while (iterator.hasNext()) {
        var row = iterator.next();
        var feature = FlatGeoBufTypeConversion.asFeature(row);
        writer.writeFeature(feature);
      }
    }
  }

  @Override
  public void close() throws Exception {
    if (iterator != null) {
      iterator.close();
    }
  }

  /**
   * An iterator over rows in a flatgeobuf file.
   */
  public static class RowIterator implements Iterator<DataRow>, AutoCloseable {

    private final FlatGeoBuf.Header header;

    private final DataSchema schema;

    private final FlatGeoBufReader reader;

    private long cursor = 0;

    /**
     * Constructs a row iterator.
     *
     * @param file the path to the flatgeobuf file
     * @param schema the schema of the table
     */
    public RowIterator(
        Path file,
        DataSchema schema) throws IOException {
      this.reader = new FlatGeoBufReader(FileChannel.open(file, StandardOpenOption.READ));
      this.header = reader.readHeader();
      this.schema = schema;
      reader.skipIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
      return cursor < header.featuresCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataRow next() {
      try {
        var feature = reader.readFeature();
        cursor++;
        return FlatGeoBufTypeConversion.asRow(schema, feature);
      } catch (IOException e) {
        throw new NoSuchElementException(e);
      }
    }

    @Override
    public void close() throws Exception {
      reader.close();
    }
  }
}
