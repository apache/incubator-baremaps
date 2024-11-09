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

package org.apache.baremaps.data.calcite;

import java.util.EnumMap;
import org.apache.baremaps.store.DataColumn.Type;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.type.SqlTypeName;
import org.locationtech.jts.geom.*;

public class SqlTypeConversion {

  static final EnumMap<Type, RelDataType> types = new EnumMap<>(Type.class);

  static {
    types.put(Type.BYTE, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.TINYINT));
    types.put(Type.BOOLEAN, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.BOOLEAN));
    types.put(Type.SHORT, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.SMALLINT));
    types.put(Type.INTEGER, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.INTEGER));
    types.put(Type.LONG, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.BIGINT));
    types.put(Type.FLOAT, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.FLOAT));
    types.put(Type.DOUBLE, new JavaTypeFactoryImpl()
        .createSqlType(SqlTypeName.DOUBLE));
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

  private SqlTypeConversion() {
    // Prevent instantiation
  }
}
