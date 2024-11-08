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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.store.*;
import org.locationtech.jts.io.WKTReader;

/**
 * A DataTable implementation that reads data from a CSV file using Jackson.
 */
public class CsvDataTable implements DataTable {

  private final DataSchema schema;
  private final File csvFile;
  private final CsvSchema csvSchema;
  private final long size;

  /**
   * Constructs a CsvDataTable with the specified schema, CSV file, header presence, and separator.
   *
   * @param schema the data schema defining the structure
   * @param csvFile the CSV file to read data from
   * @param hasHeader whether the CSV file includes a header row
   * @param separator the character used to separate columns in the CSV file
   * @throws IOException if an I/O error occurs
   */
  public CsvDataTable(DataSchema schema, File csvFile, boolean hasHeader, char separator)
      throws IOException {
    this.schema = schema;
    this.csvFile = csvFile;
    this.csvSchema = buildCsvSchema(schema, hasHeader, separator);
    this.size = calculateSize();
  }

  /**
   * Builds the CsvSchema for Jackson based on the provided DataSchema, header presence, and
   * separator.
   *
   * @param dataSchema the data schema
   * @param hasHeader whether the CSV file includes a header row
   * @param separator the character used to separate columns
   * @return the CsvSchema for Jackson
   */
  private CsvSchema buildCsvSchema(DataSchema dataSchema, boolean hasHeader, char separator) {
    CsvSchema.Builder builder = CsvSchema.builder();
    for (DataColumn column : dataSchema.columns()) {
      builder.addColumn(column.name());
    }
    return builder.setUseHeader(hasHeader).setColumnSeparator(separator).build();
  }

  /**
   * Calculates the number of rows in the CSV file.
   *
   * @return the number of rows
   * @throws IOException if an I/O error occurs
   */
  private long calculateSize() throws IOException {
    try (var parser = new CsvMapper().readerFor(Map.class)
        .with(csvSchema)
        .createParser(csvFile)) {
      long rowCount = 0;
      while (parser.nextToken() != null) {
        if (parser.currentToken() == JsonToken.START_OBJECT) {
          rowCount++;
        }
      }
      return rowCount;
    }
  }

  @Override
  public DataSchema schema() {
    return schema;
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
  public Iterator<DataRow> iterator() {
    try {
      CsvMapper csvMapper = new CsvMapper();
      JsonParser parser = csvMapper.readerFor(Map.class)
          .with(csvSchema)
          .createParser(csvFile);

      Iterator<Map<String, String>> csvIterator = csvMapper.readerFor(Map.class)
          .with(csvSchema)
          .readValues(parser);

      return new Iterator<>() {
        @Override
        public boolean hasNext() {
          return csvIterator.hasNext();
        }

        @Override
        public DataRow next() {
          Map<String, String> csvRow = csvIterator.next();
          DataRow dataRow = schema.createRow();

          for (int i = 0; i < schema.columns().size(); i++) {
            DataColumn column = schema.columns().get(i);
            String columnName = column.name();
            String value = csvRow.get(columnName);

            if (value != null) {
              Object parsedValue = parseValue(column, value);
              dataRow.set(i, parsedValue);
            } else {
              dataRow.set(i, null);
            }
          }
          return dataRow;
        }
      };

    } catch (IOException e) {
      throw new DataStoreException("Error reading CSV file", e);
    }
  }

  /**
   * Parses the string value from the CSV according to the column type.
   *
   * @param column the data column
   * @param value the string value from the CSV
   * @return the parsed value
   */
  private Object parseValue(DataColumn column, String value) {
    DataColumn.Type type = column.type();
    try {
      if (value == null || value.isEmpty()) {
        return null;
      }
        return switch (type) {
            case STRING -> value;
            case INTEGER -> Integer.parseInt(value);
            case LONG -> Long.parseLong(value);
            case FLOAT -> Float.parseFloat(value);
            case DOUBLE -> Double.parseDouble(value);
            case BOOLEAN -> Boolean.parseBoolean(value);
            case GEOMETRY, POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON,
                 GEOMETRYCOLLECTION -> {
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
  public Spliterator<DataRow> spliterator() {
    return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
  }

  @Override
  public Stream<DataRow> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}
