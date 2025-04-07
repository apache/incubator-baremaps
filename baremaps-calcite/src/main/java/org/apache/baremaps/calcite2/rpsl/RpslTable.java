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

package org.apache.baremaps.calcite2.rpsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.baremaps.rpsl.RpslObject;
import org.apache.baremaps.rpsl.RpslReader;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Calcite table implementation for RPSL data. This table reads data from a RPSL file and makes it
 * available through the Apache Calcite framework for SQL querying.
 */
public class RpslTable extends AbstractTable implements ScannableTable {

  private static final Logger logger = LoggerFactory.getLogger(RpslTable.class);

  private final File file;
  private final DataSchema schema;
  private RelDataType rowType;

  /**
   * Constructs a RpslTable with the specified file.
   *
   * @param file the RPSL file to read data from
   * @throws IOException if an I/O error occurs
   */
  public RpslTable(File file) throws IOException {
    this.file = file;
    this.schema = buildSchema(file.getName());
  }

  /**
   * Builds a schema for the RPSL data.
   *
   * @param name the name of the schema
   * @return the schema
   */
  private DataSchema buildSchema(String name) {
    var columns = new ArrayList<DataColumn>();

    // Add standard RPSL columns
    columns.add(new DataColumnFixed("type", Cardinality.REQUIRED, Type.STRING));
    columns.add(new DataColumnFixed("id", Cardinality.REQUIRED, Type.STRING));
    columns.add(new DataColumnFixed("inetnum", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("inet6num", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("netname", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("descr", Cardinality.REPEATED, Type.STRING));
    columns.add(new DataColumnFixed("country", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("admin-c", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("tech-c", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("status", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("mnt-by", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("created", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("last-modified", Cardinality.OPTIONAL, Type.STRING));
    columns.add(new DataColumnFixed("changed", Cardinality.REPEATED, Type.STRING));

    return new DataSchema(name, columns);
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    if (rowType == null) {
      rowType = createRowType(typeFactory);
    }
    return rowType;
  }

  /**
   * Creates the row type (schema) for the RPSL data.
   *
   * @param typeFactory the type factory
   * @return the RelDataType representing the schema
   */
  private RelDataType createRowType(RelDataTypeFactory typeFactory) {
    RelDataTypeFactory.Builder builder = typeFactory.builder();

    // Define the columns based on the schema
    for (DataColumn column : schema.columns()) {
      if (column.cardinality() == Cardinality.REPEATED) {
        // For repeated fields, use a list type
        builder.add(column.name(),
            typeFactory.createArrayType(typeFactory.createSqlType(SqlTypeName.VARCHAR), -1));
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
        return new RpslEnumerator(file, schema);
      }
    };
  }

  /**
   * Enumerator for RPSL data.
   */
  private static class RpslEnumerator implements Enumerator<Object[]> {
    private final File file;
    private final DataSchema schema;
    private InputStream inputStream;
    private Iterator<RpslObject> rpslObjectIterator;
    private RpslObject current;

    public RpslEnumerator(File file, DataSchema schema) {
      this.file = file;
      this.schema = schema;
      initialize();
    }

    private void initialize() {
      try {
        this.inputStream = new FileInputStream(file);
        RpslReader rpslReader = new RpslReader();
        this.rpslObjectIterator = rpslReader.read(inputStream).iterator();
      } catch (IOException e) {
        throw new RuntimeException("Failed to initialize RPSL iterator", e);
      }
    }

    @Override
    public Object[] current() {
      if (current == null) {
        return null;
      }
      return createRow(current);
    }

    @Override
    public boolean moveNext() {
      if (rpslObjectIterator.hasNext()) {
        current = rpslObjectIterator.next();
        return true;
      }
      return false;
    }

    @Override
    public void reset() {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
        initialize();
      } catch (IOException e) {
        throw new RuntimeException("Failed to reset RPSL iterator", e);
      }
    }

    @Override
    public void close() {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException e) {
        // Ignore
      }
    }

    private Object[] createRow(RpslObject rpslObject) {
      Object[] row = new Object[schema.columns().size()];

      for (int i = 0; i < schema.columns().size(); i++) {
        DataColumn column = schema.columns().get(i);
        String columnName = column.name().toLowerCase();

        if (columnName.equals("type")) {
          row[i] = rpslObject.type();
        } else if (columnName.equals("id")) {
          row[i] = rpslObject.id();
        } else {
          switch (column.cardinality()) {
            case REQUIRED:
            case OPTIONAL:
              row[i] = rpslObject.first(columnName).orElse(null);
              break;
            case REPEATED:
              List<String> values = rpslObject.all(columnName);
              row[i] = values.isEmpty() ? null : values;
              break;
          }
        }
      }

      return row;
    }
  }
}
