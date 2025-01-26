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

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.locationtech.jts.io.WKTReader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A DataTable implementation that reads data from a CSV file using Jackson.
 */
public class CsvDataTable implements DataTable {

    private final File file;
    private final CsvSchema csvSchema;
    private final DataSchema dataSchema;

    private final long size;
    private MappingIterator<Map<String, String>> csvIterator;

    /**
     * Constructs a CsvDataTable with the specified schema, CSV file, header presence, and separator.
     *
     * @param name      the name of the table
     * @param csvFile   the CSV file to read data from
     * @param header    whether the CSV file includes a header row
     * @param separator the separator used in the CSV file
     * @throws IOException if an I/O error occurs
     */
    public CsvDataTable(String name, File csvFile, boolean header, char separator) throws IOException {
        this.file = csvFile;

        // Iterate over all records to infer the csv schema and calculate the size of the table
        CsvSchema csvSchema = CsvSchema.emptySchema()
                .withUseHeader(header)
                .withColumnSeparator(separator);

        try (MappingIterator<Object> iterator = new CsvMapper()
                .readerFor(Object.class)
                .with(csvSchema)
                .readValues(csvFile)) {

            int count = 0;
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }

            this.csvSchema = (CsvSchema) iterator.getParserSchema();
            this.size = count;
        } catch (IOException e) {
            throw new DataStoreException("Error reading CSV file", e);
        }

        // Map the csv schema to a data schema
        List<DataColumn> columns = new ArrayList<>();
        for (String columnName : this.csvSchema.getColumnNames()) {
            switch (this.csvSchema.column(columnName).getType()) {
                case STRING -> columns
                        .add(new DataColumnFixed(columnName, Cardinality.REQUIRED, ColumnType.STRING));
                case NUMBER -> columns
                        .add(new DataColumnFixed(columnName, Cardinality.REQUIRED, ColumnType.DOUBLE));
                case BOOLEAN -> columns
                        .add(new DataColumnFixed(columnName, Cardinality.REQUIRED, ColumnType.BOOLEAN));
                case ARRAY -> columns
                        .add(new DataColumnFixed(columnName, Cardinality.REPEATED, ColumnType.STRING));
                default -> throw new IllegalArgumentException(
                        "Unsupported column type: " + csvSchema.column(columnName).getType());
            }
        }
        this.dataSchema = new DataSchemaImpl(name, columns);
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
    public Iterator<DataRow> iterator() {
        try {

            csvIterator =  new CsvMapper()
                    .readerFor(Map.class)
                    .with(csvSchema)
                    .readValues(file);

            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return csvIterator.hasNext();
                }

                @Override
                public DataRow next() {
                    Map<String, String> csvRow = csvIterator.next();
                    DataRow dataRow = dataSchema.createRow();

                    for (int i = 0; i < dataSchema.columns().size(); i++) {
                        DataColumn column = dataSchema.columns().get(i);
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
     * @param value  the string value from the CSV
     * @return the parsed value
     */
    private Object parseValue(DataColumn column, String value) {
        ColumnType type = column.type();
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

    @Override
    public void close() throws IOException {
        if (csvIterator != null) {
            csvIterator.close();
        }
    }
}
