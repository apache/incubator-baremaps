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

package org.apache.baremaps.calcite2.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.calcite.DataColumn;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.calcite.DataColumnFixed;
import org.apache.baremaps.calcite.DataRow;
import org.apache.baremaps.calcite.DataSchema;
import org.apache.baremaps.shapefile.ShapefileInputStream;
import org.apache.baremaps.shapefile.ShapefileReader;
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
 * A Calcite table implementation for shapefile data. This table reads data from a shapefile and makes
 * it available through the Apache Calcite framework for SQL querying.
 */
public class ShapefileTable extends AbstractTable implements ScannableTable {

  private static final Logger logger = LoggerFactory.getLogger(ShapefileTable.class);

  private final File file;
  private final ShapefileReader shapeFile;
  private final DataSchema schema;
  private RelDataType rowType;

  /**
   * Constructs a ShapefileTable with the specified file.
   *
   * @param file the shapefile to read data from
   * @throws IOException if an I/O error occurs
   */
  public ShapefileTable(File file) throws IOException {
    this.file = file;
    this.shapeFile = new ShapefileReader(file.getPath());
    this.schema = buildSchema(file.getName());
  }

  /**
   * Builds a schema from the shapefile.
   *
   * @param name the name of the schema
   * @return the schema
   */
  private DataSchema buildSchema(String name) {
    var columns = new ArrayList<DataColumn>();
    
    // Add columns from the shapefile's database fields
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

    // Add geometry column
    columns.add(new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY));

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
   * Creates the row type (schema) for the shapefile data.
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
        return new ShapefileEnumerator(file, schema);
      }
    };
  }

  /**
   * Enumerator for shapefile data.
   */
  private static class ShapefileEnumerator implements Enumerator<Object[]> {
    private final File file;
    private final DataSchema schema;
    private ShapefileInputStream shapefileInputStream;
    private List<Object> current;

    public ShapefileEnumerator(File file, DataSchema schema) {
      this.file = file;
      this.schema = schema;
      initialize();
    }

    private void initialize() {
      try {
        var shapeFile = new ShapefileReader(file.getPath());
        this.shapefileInputStream = shapeFile.read();
      } catch (IOException e) {
        throw new RuntimeException("Failed to initialize shapefile iterator", e);
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
        current = shapefileInputStream.readRow();
        return current != null;
      } catch (IOException e) {
        logger.error("Error reading shapefile row", e);
        return false;
      }
    }

    @Override
    public void reset() {
      try {
        shapefileInputStream.close();
        initialize();
      } catch (IOException e) {
        throw new RuntimeException("Failed to reset shapefile iterator", e);
      }
    }

    @Override
    public void close() {
      try {
        if (shapefileInputStream != null) {
          shapefileInputStream.close();
        }
      } catch (IOException e) {
        // Ignore
      }
    }
  }
} 