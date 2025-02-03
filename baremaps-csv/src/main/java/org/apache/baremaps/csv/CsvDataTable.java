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

package org.apache.baremaps.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.locationtech.jts.io.WKTReader;

public class CsvDataTable implements DataTable {

  private final File file;
  private final CsvSchema csvSchema;
  private final DataSchema dataSchema;
  private final long size;
  private final boolean hasHeader;

  /**
   * Constructor that infers the CSV file’s columns.
   * <p>
   * This constructor accepts a {@code hasHeader} parameter. If {@code hasHeader} is true, the
   * file’s header is used to pick column names; if false, the first row is used to determine the
   * number of columns, and generic names ("column1", "column2", etc.) are generated.
   *
   * @param name the name of the table
   * @param csvFile the CSV file to read data from
   * @param separator the separator used in the CSV file
   * @param hasHeader whether the CSV file includes a header row
   * @throws IOException if an I/O error occurs
   */
  public CsvDataTable(String name, File csvFile, char separator, boolean hasHeader)
      throws IOException {
    this(name, csvFile, separator, inferColumns(csvFile, separator, hasHeader), hasHeader);
  }

  /**
   * Constructor that uses the provided column definitions.
   *
   * @param name the name of the table
   * @param csvFile the CSV file to read data from
   * @param separator the separator used in the CSV file
   * @param columns the list of columns (data types, names, etc.)
   * @param hasHeader indicates whether the CSV file has a header row
   * @throws IOException if an I/O error occurs
   */
  public CsvDataTable(String name, File csvFile, char separator, List<DataColumn> columns,
      boolean hasHeader)
      throws IOException {
    this.file = csvFile;
    this.hasHeader = hasHeader;
    CsvMapper mapper = new CsvMapper();
    CsvSchema schema = buildSchema(columns, separator, hasHeader);

    int rowCount = 0;
    try (MappingIterator<Map<String, String>> iterator = mapper
        .readerFor(Map.class)
        .with(schema)
        .readValues(file)) {
      while (iterator.hasNext()) {
        iterator.next();
        rowCount++;
      }
      // If a header is present, update the schema from the parser (if available).
      if (hasHeader) {
        CsvSchema inferred = (CsvSchema) iterator.getParserSchema();
        if (inferred != null) {
          schema = inferred;
        }
      }
    } catch (IOException e) {
      throw new DataStoreException("Error reading CSV file", e);
    }
    this.size = rowCount;
    this.csvSchema = schema;
    this.dataSchema = new DataSchemaImpl(name, columns);
  }

  /**
   * Builds a CSV schema from the given column definitions.
   *
   * @param columns the list of columns
   * @param separator the column separator
   * @param hasHeader whether the CSV file has a header row
   * @return the CSV schema
   */
  private static CsvSchema buildSchema(List<DataColumn> columns, char separator,
      boolean hasHeader) {
    if (hasHeader) {
      return CsvSchema.emptySchema().withHeader().withColumnSeparator(separator);
    } else {
      CsvSchema.Builder builder = CsvSchema.builder();
      for (DataColumn col : columns) {
        builder.addColumn(col.name());
      }
      return builder.setColumnSeparator(separator).build();
    }
  }

  /**
   * Infers columns from the CSV file.
   * <p>
   * If {@code hasHeader} is true, this method reads the header row to extract column names.
   * Otherwise, it reads the first row to determine the number of columns and generates names.
   *
   * @param csvFile the CSV file
   * @param separator the column separator
   * @param hasHeader whether the CSV file includes a header row
   * @return a list of columns (all of type STRING)
   * @throws IOException if an I/O error occurs
   */
  private static List<DataColumn> inferColumns(File csvFile, char separator, boolean hasHeader)
      throws IOException {
    CsvMapper mapper = new CsvMapper();
    List<DataColumn> columns = new ArrayList<>();
    if (hasHeader) {
      // Read the header to infer column names.
      CsvSchema baseSchema = CsvSchema.emptySchema().withHeader().withColumnSeparator(separator);
      try (MappingIterator<Map<String, String>> iterator = mapper.readerFor(Map.class)
          .with(baseSchema)
          .readValues(csvFile)) {
        // Consume one record so that the header is processed.
        if (iterator.hasNext()) {
          iterator.next();
        }
        CsvSchema inferred = (CsvSchema) iterator.getParserSchema();
        if (inferred == null) {
          throw new DataStoreException("Failed to infer CSV schema from header.");
        }
        for (String colName : inferred.getColumnNames()) {
          columns.add(new DataColumnFixed(colName, Cardinality.OPTIONAL, ColumnType.STRING));
        }
      }
    } else {
      // No header: read the first row to determine the number of columns.
      CsvSchema baseSchema = CsvSchema.emptySchema().withColumnSeparator(separator);
      try (MappingIterator<List<String>> iterator = mapper.readerFor(List.class)
          .with(baseSchema)
          .readValues(csvFile)) {
        if (iterator.hasNext()) {
          List<String> firstRow = iterator.next();
          int numColumns = firstRow.size();
          for (int i = 0; i < numColumns; i++) {
            columns.add(
                new DataColumnFixed("column" + (i + 1), Cardinality.OPTIONAL, ColumnType.STRING));
          }
        } else {
          throw new DataStoreException("CSV file is empty; cannot infer column names.");
        }
      }
    }
    return columns;
  }

  @Override
  public DataSchema schema() {
    return dataSchema;
  }

  @Override
  public boolean add(DataRow row) {
    throw new UnsupportedOperationException("Adding rows is not supported.");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("Clearing rows is not supported.");
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public java.util.Iterator<DataRow> iterator() {
    try {
      CsvMapper mapper = new CsvMapper();
      MappingIterator<Map<String, String>> localIterator = mapper.readerFor(Map.class)
          .with(csvSchema)
          .readValues(file);

      return new java.util.Iterator<>() {
        @Override
        public boolean hasNext() {
          boolean has = localIterator.hasNext();
          if (!has) {
            try {
              localIterator.close();
            } catch (IOException e) {
              throw new DataStoreException("Error closing CSV iterator", e);
            }
          }
          return has;
        }

        @Override
        public DataRow next() {
          if (!hasNext()) {
            throw new java.util.NoSuchElementException();
          }
          Map<String, String> csvRow = localIterator.next();
          DataRow dataRow = dataSchema.createRow();
          for (int i = 0; i < dataSchema.columns().size(); i++) {
            DataColumn column = dataSchema.columns().get(i);
            String value = csvRow.get(column.name());
            dataRow.set(i, (value == null || value.isEmpty()) ? null : parseValue(column, value));
          }
          return dataRow;
        }
      };

    } catch (IOException e) {
      throw new DataStoreException("Error reading CSV file", e);
    }
  }

  private static Object parseValue(DataColumn column, String value) {
        ColumnType type = column.type();
        try {
            return switch (type) {
                case STRING -> value;
                case INTEGER -> Integer.parseInt(value);
                case LONG -> Long.parseLong(value);
                case FLOAT -> Float.parseFloat(value);
                case DOUBLE -> Double.parseDouble(value);
                case BOOLEAN -> Boolean.parseBoolean(value);
                case GEOMETRY, POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON, GEOMETRYCOLLECTION -> {
                    WKTReader reader = new WKTReader();
                    yield reader.read(value);
                }
                default -> throw new IllegalArgumentException("Unsupported column type: " + type);
            };
        } catch (Exception e) {
            throw new DataStoreException("Error parsing value for column " + column.name(), e);
        }
    }

  @Override
  public java.util.Spliterator<DataRow> spliterator() {
    return java.util.Spliterators.spliteratorUnknownSize(iterator(), java.util.Spliterator.ORDERED);
  }

  @Override
  public java.util.stream.Stream<DataRow> stream() {
    return java.util.stream.StreamSupport.stream(spliterator(), false);
  }

  @Override
  public void close() throws IOException {
    // No persistent resources to close.
  }
}
