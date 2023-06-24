/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.database.calcite;

import java.util.Collection;
import java.util.EnumMap;
import org.apache.baremaps.database.collection.DataCollectionAdapter;
import org.apache.baremaps.database.table.DataColumn;
import org.apache.baremaps.database.table.DataColumn.Type;
import org.apache.baremaps.database.table.DataTable;
import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.locationtech.jts.geom.*;

/**
 * A simple table based on a list.
 */
class SqlDataTable extends AbstractTable implements ScannableTable {

  private static final EnumMap<Type, RelDataType> types = new EnumMap<>(Type.class);

  static {
    types.put(Type.BYTE, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.TINYINT));
    types.put(Type.BYTE_ARRAY, new JavaTypeFactoryImpl()
        .createArrayType(new JavaTypeFactoryImpl().createSqlType(SqlTypeName.TINYINT), -1));
    types.put(Type.BOOLEAN, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.BOOLEAN));
    types.put(Type.BOOLEAN_ARRAY, new JavaTypeFactoryImpl()
        .createArrayType(new JavaTypeFactoryImpl().createSqlType(SqlTypeName.BOOLEAN), -1));
    types.put(Type.SHORT, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.SMALLINT));
    types.put(Type.SHORT_ARRAY, new JavaTypeFactoryImpl()
        .createArrayType(new JavaTypeFactoryImpl().createSqlType(SqlTypeName.SMALLINT), -1));
    types.put(Type.INTEGER, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.INTEGER));
    types.put(Type.INTEGER_ARRAY, new JavaTypeFactoryImpl()
        .createArrayType(new JavaTypeFactoryImpl().createSqlType(SqlTypeName.INTEGER), -1));
    types.put(Type.LONG, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.BIGINT));
    types.put(Type.LONG_ARRAY, new JavaTypeFactoryImpl()
        .createArrayType(new JavaTypeFactoryImpl().createSqlType(SqlTypeName.BIGINT), -1));
    types.put(Type.FLOAT, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.FLOAT));
    types.put(Type.FLOAT_ARRAY, new JavaTypeFactoryImpl()
        .createArrayType(new JavaTypeFactoryImpl().createSqlType(SqlTypeName.FLOAT), -1));
    types.put(Type.DOUBLE, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.DOUBLE));
    types.put(Type.DOUBLE_ARRAY, new JavaTypeFactoryImpl()
        .createArrayType(new JavaTypeFactoryImpl().createSqlType(SqlTypeName.DOUBLE), -1));
    types.put(Type.STRING, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.VARCHAR));
    types.put(Type.GEOMETRY, new JavaTypeFactoryImpl()
        .createJavaType(Geometry.class));
    types.put(Type.POINT, new JavaTypeFactoryImpl()
        .createJavaType(Point.class));
    types.put(Type.LINESTRING, new JavaTypeFactoryImpl()
        .createJavaType(LineString.class));
    types.put(Type.POLYGON, new JavaTypeFactoryImpl()
        .createJavaType(Polygon.class));
    types.put(Type.MULTIPOINT, new JavaTypeFactoryImpl()
        .createJavaType(MultiPoint.class));
    types.put(Type.MULTILINESTRING, new JavaTypeFactoryImpl()
        .createJavaType(MultiLineString.class));
    types.put(Type.MULTIPOLYGON, new JavaTypeFactoryImpl()
        .createJavaType(MultiPolygon.class));
    types.put(Type.GEOMETRYCOLLECTION, new JavaTypeFactoryImpl()
        .createJavaType(GeometryCollection.class));
  }

  private final DataTable table;

  SqlDataTable(DataTable table) {
    this.table = table;
  }

  @Override
  public Enumerable<Object[]> scan(final DataContext root) {
    Collection<Object[]> collection =
        new DataCollectionAdapter<>(table, row -> row.values().toArray());
    return Linq4j.asEnumerable(collection);
  }

  @Override
  public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
    var rowType = new RelDataTypeFactory.Builder(typeFactory);
    for (DataColumn column : table.rowType().columns()) {
      rowType.add(column.name(), types.get(column.type()));
    }
    return rowType.build();
  }
}
