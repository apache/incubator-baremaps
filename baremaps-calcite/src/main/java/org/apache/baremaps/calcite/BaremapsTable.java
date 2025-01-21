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

package org.apache.baremaps.calcite;

import java.util.EnumMap;
import org.apache.baremaps.store.DataColumn;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.apache.baremaps.store.DataTable;
import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.locationtech.jts.geom.*;

public class BaremapsTable extends AbstractTable implements ScannableTable {

  private static final EnumMap<ColumnType, RelDataType> types = new EnumMap<>(ColumnType.class);

  static {
    types.put(ColumnType.BYTE, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.TINYINT));
    types.put(ColumnType.BOOLEAN, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.BOOLEAN));
    types.put(ColumnType.SHORT, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.SMALLINT));
    types.put(ColumnType.INTEGER, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.INTEGER));
    types.put(ColumnType.LONG, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.BIGINT));
    types.put(ColumnType.FLOAT, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.FLOAT));
    types.put(ColumnType.DOUBLE, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.DOUBLE));
    types.put(ColumnType.STRING, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.VARCHAR));
    types.put(ColumnType.GEOMETRY, new JavaTypeFactoryImpl()
        .createJavaType(Geometry.class));
    types.put(ColumnType.POINT, new JavaTypeFactoryImpl()
        .createJavaType(Point.class));
    types.put(ColumnType.LINESTRING, new JavaTypeFactoryImpl()
        .createJavaType(LineString.class));
    types.put(ColumnType.POLYGON, new JavaTypeFactoryImpl()
        .createJavaType(Polygon.class));
    types.put(ColumnType.MULTIPOINT, new JavaTypeFactoryImpl()
        .createJavaType(MultiPoint.class));
    types.put(ColumnType.MULTILINESTRING, new JavaTypeFactoryImpl()
        .createJavaType(MultiLineString.class));
    types.put(ColumnType.MULTIPOLYGON, new JavaTypeFactoryImpl()
        .createJavaType(MultiPolygon.class));
    types.put(ColumnType.GEOMETRYCOLLECTION, new JavaTypeFactoryImpl()
        .createJavaType(GeometryCollection.class));
  }

  private final DataTable table;

  private final RelProtoDataType protoRowType;

  private RelDataType rowType;

  public BaremapsTable(DataTable table) {
    this.table = table;
    this.protoRowType = null;
  }

  public BaremapsTable(DataTable table, RelProtoDataType protoRowType) {
    this.table = table;
    this.protoRowType = protoRowType;
  }

  @Override
  public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
    if (rowType == null) {
      rowType = createRowType(typeFactory);
    }
    return rowType;
  }

  private RelDataType createRowType(RelDataTypeFactory typeFactory) {
    var rowTypeBuilder = new RelDataTypeFactory.Builder(typeFactory);
    for (DataColumn column : table.schema().columns()) {
      rowTypeBuilder.add(column.name(), types.get(column.type()));
    }
    return rowTypeBuilder.build();
  }

  @Override
  public Enumerable<Object[]> scan(final DataContext root) {
    return Linq4j.asEnumerable(() -> table.stream()
        .map(row -> row.values().toArray())
        .iterator());
  }
}
