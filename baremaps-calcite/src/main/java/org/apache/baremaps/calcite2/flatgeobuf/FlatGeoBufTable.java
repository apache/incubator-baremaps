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

package org.apache.baremaps.calcite2.flatgeobuf;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.calcite.DataColumn;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.calcite.DataColumnFixed;
import org.apache.baremaps.calcite.DataSchema;
import org.apache.baremaps.flatgeobuf.FlatGeoBuf;
import org.apache.baremaps.flatgeobuf.FlatGeoBufReader;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Calcite table implementation for FlatGeoBuf data. This table reads data from a FlatGeoBuf file
 * and makes it available through the Apache Calcite framework for SQL querying.
 */
public class FlatGeoBufTable extends AbstractTable implements ScannableTable {

  private static final Logger logger = LoggerFactory.getLogger(FlatGeoBufTable.class);

  private final File file;
  private final FlatGeoBufReader reader;
  private final DataSchema schema;
  private RelDataType rowType;

  /**
   * Constructs a FlatGeoBufTable with the specified file.
   *
   * @param file the FlatGeoBuf file to read data from
   * @throws IOException if an I/O error occurs
   */
  public FlatGeoBufTable(File file) throws IOException {
    this.file = file;
    this.reader = new FlatGeoBufReader(FileChannel.open(file.toPath(), StandardOpenOption.READ));
    this.schema = buildSchema(file.getName());
  }

  /**
   * Builds a schema from the FlatGeoBuf file.
   *
   * @param name the name of the schema
   * @return the schema
   */
  private DataSchema buildSchema(String name) {
    var columns = new ArrayList<DataColumn>();

    try {
      // Read the header to get the schema information
      FlatGeoBuf.Header header = reader.readHeader();

      // Add columns from the FlatGeoBuf header
      for (FlatGeoBuf.Column column : header.columns()) {
        var columnName = column.name();
        var columnType = convertColumnType(column.type());
        columns.add(new DataColumnFixed(columnName,
            column.nullable() ? Cardinality.OPTIONAL : Cardinality.REQUIRED,
            columnType));
      }
    } catch (IOException e) {
      logger.error("Error reading FlatGeoBuf header", e);
      throw new RuntimeException("Failed to read FlatGeoBuf header", e);
    }

    // Add geometry column
    columns.add(new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY));

    return new DataSchema(name, columns);
  }

  /**
   * Converts FlatGeoBuf column type to DataColumn.Type
   * 
   * @param columnType the FlatGeoBuf column type
   * @return the corresponding DataColumn.Type
   */
  private Type convertColumnType(FlatGeoBuf.ColumnType columnType) {
    return switch (columnType) {
      case BYTE, UBYTE -> Type.BYTE;
      case BOOL -> Type.BOOLEAN;
      case SHORT, USHORT -> Type.SHORT;
      case INT, UINT -> Type.INTEGER;
      case LONG, ULONG -> Type.LONG;
      case FLOAT -> Type.FLOAT;
      case DOUBLE -> Type.DOUBLE;
      case STRING -> Type.STRING;
      case JSON, DATETIME, BINARY -> Type.STRING; // Map unsupported types to STRING
    };
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    if (rowType == null) {
      rowType = createRowType(typeFactory);
    }
    return rowType;
  }

  /**
   * Creates the row type (schema) for the FlatGeoBuf data.
   *
   * @param typeFactory the type factory
   * @return the RelDataType representing the schema
   */
  private RelDataType createRowType(RelDataTypeFactory typeFactory) {
    RelDataTypeFactory.Builder builder = typeFactory.builder();

    // Define the columns based on the schema
    for (DataColumn column : schema.columns()) {
      if (column.type() == Type.GEOMETRY) {
        builder.add(column.name(), typeFactory.createJavaType(Geometry.class));
      } else {
        builder.add(column.name(), typeFactory.createSqlType(SqlTypeName.VARCHAR));
      }
    }

    return builder.build();
  }

  @Override
  public Enumerable<Object[]> scan(DataContext root) {
    return new AbstractEnumerable<Object[]>() {
      @Override
      public Enumerator<Object[]> enumerator() {
        return new FlatGeoBufEnumerator(file, schema);
      }
    };
  }

  /**
   * Enumerator for FlatGeoBuf data.
   */
  private static class FlatGeoBufEnumerator implements Enumerator<Object[]> {
    private final File file;
    private final DataSchema schema;
    private FlatGeoBufReader reader;
    private List<Object> current;
    private long cursor = 0;
    private long featureCount;

    public FlatGeoBufEnumerator(File file, DataSchema schema) {
      this.file = file;
      this.schema = schema;
      initialize();
    }

    private void initialize() {
      try {
        reader = new FlatGeoBufReader(FileChannel.open(file.toPath(), StandardOpenOption.READ));
        FlatGeoBuf.Header header = reader.readHeader();
        featureCount = header.featuresCount();
        reader.skipIndex();
      } catch (IOException e) {
        throw new RuntimeException("Failed to initialize FlatGeoBuf iterator", e);
      }
    }

    @Override
    public Object[] current() {
      if (current == null) {
        return null;
      }
      return current.toArray();
    }

    @Override
    public boolean moveNext() {
      try {
        if (cursor >= featureCount) {
          return false;
        }

        FlatGeoBuf.Feature feature = reader.readFeature();
        cursor++;

        // Convert feature to row
        List<Object> values = new ArrayList<>();

        // Add geometry
        values.add(feature.geometry());

        // Add properties
        if (!feature.properties().isEmpty()) {
          values.addAll(feature.properties());
        }

        current = values;
        return true;
      } catch (IOException e) {
        logger.error("Error reading FlatGeoBuf row", e);
        return false;
      }
    }

    @Override
    public void reset() {
      try {
        reader.close();
        initialize();
      } catch (IOException e) {
        throw new RuntimeException("Failed to reset FlatGeoBuf iterator", e);
      }
    }

    @Override
    public void close() {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        // Ignore
      }
    }
  }
}
