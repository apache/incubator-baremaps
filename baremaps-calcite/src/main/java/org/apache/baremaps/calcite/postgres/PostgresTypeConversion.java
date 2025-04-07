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

package org.apache.baremaps.calcite.postgres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.calcite.data.DataColumn;
import org.apache.baremaps.calcite.data.DataSchema;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;

/**
 * Utility class for converting between PostgreSQL/PostGIS types and Calcite types.
 */
public class PostgresTypeConversion {

  private PostgresTypeConversion() {
    // Prevent instantiation
  }

  private static final Map<String, SqlTypeName> POSTGRES_TO_SQL_TYPE = new HashMap<>();

  static {
    // Standard PostgreSQL types
    POSTGRES_TO_SQL_TYPE.put("varchar", SqlTypeName.VARCHAR);
    POSTGRES_TO_SQL_TYPE.put("char", SqlTypeName.CHAR);
    POSTGRES_TO_SQL_TYPE.put("text", SqlTypeName.VARCHAR);
    POSTGRES_TO_SQL_TYPE.put("smallint", SqlTypeName.SMALLINT);
    POSTGRES_TO_SQL_TYPE.put("int2", SqlTypeName.SMALLINT);
    POSTGRES_TO_SQL_TYPE.put("integer", SqlTypeName.INTEGER);
    POSTGRES_TO_SQL_TYPE.put("int4", SqlTypeName.INTEGER);
    POSTGRES_TO_SQL_TYPE.put("bigint", SqlTypeName.BIGINT);
    POSTGRES_TO_SQL_TYPE.put("int8", SqlTypeName.BIGINT);
    POSTGRES_TO_SQL_TYPE.put("decimal", SqlTypeName.DECIMAL);
    POSTGRES_TO_SQL_TYPE.put("numeric", SqlTypeName.DECIMAL);
    POSTGRES_TO_SQL_TYPE.put("real", SqlTypeName.REAL);
    POSTGRES_TO_SQL_TYPE.put("float4", SqlTypeName.FLOAT);
    POSTGRES_TO_SQL_TYPE.put("double precision", SqlTypeName.DOUBLE);
    POSTGRES_TO_SQL_TYPE.put("float8", SqlTypeName.DOUBLE);
    POSTGRES_TO_SQL_TYPE.put("boolean", SqlTypeName.BOOLEAN);
    POSTGRES_TO_SQL_TYPE.put("bool", SqlTypeName.BOOLEAN);
    POSTGRES_TO_SQL_TYPE.put("date", SqlTypeName.DATE);
    POSTGRES_TO_SQL_TYPE.put("time", SqlTypeName.TIME);
    POSTGRES_TO_SQL_TYPE.put("timestamp", SqlTypeName.TIMESTAMP);
    POSTGRES_TO_SQL_TYPE.put("timestamptz", SqlTypeName.TIMESTAMP_WITH_LOCAL_TIME_ZONE);
    POSTGRES_TO_SQL_TYPE.put("uuid", SqlTypeName.VARCHAR);
    POSTGRES_TO_SQL_TYPE.put("json", SqlTypeName.VARCHAR);
    POSTGRES_TO_SQL_TYPE.put("jsonb", SqlTypeName.VARCHAR);
    POSTGRES_TO_SQL_TYPE.put("bytea", SqlTypeName.BINARY);
    POSTGRES_TO_SQL_TYPE.put("interval", SqlTypeName.INTERVAL_DAY_SECOND);

    // PostGIS specific types
    POSTGRES_TO_SQL_TYPE.put("geometry", SqlTypeName.GEOMETRY);
    POSTGRES_TO_SQL_TYPE.put("geography", SqlTypeName.GEOMETRY);
    POSTGRES_TO_SQL_TYPE.put("box2d", SqlTypeName.OTHER);
    POSTGRES_TO_SQL_TYPE.put("box3d", SqlTypeName.OTHER);
  }

  /**
   * Converts a PostgreSQL data type to a Calcite RelDataType.
   *
   * @param typeFactory the type factory
   * @param postgresType the PostgreSQL data type
   * @return the Calcite RelDataType
   */
  public static RelDataType postgresTypeToRelDataType(RelDataTypeFactory typeFactory,
      String postgresType) {
    SqlTypeName sqlTypeName = POSTGRES_TO_SQL_TYPE.getOrDefault(postgresType, SqlTypeName.ANY);
    return typeFactory.createSqlType(sqlTypeName);
  }

  /**
   * Converts a DataSchema to a Calcite RelDataType.
   *
   * @param typeFactory the type factory
   * @param schema the schema
   * @return the RelDataType
   */
  public static RelDataType toRelDataType(RelDataTypeFactory typeFactory, DataSchema schema) {
    List<Map.Entry<String, RelDataType>> fields = new ArrayList<>();

    for (DataColumn column : schema.columns()) {
      boolean nullable = column.cardinality() == DataColumn.Cardinality.OPTIONAL;
      RelDataType fieldType = column.relDataType();

      if (nullable) {
        fieldType = typeFactory.createTypeWithNullability(fieldType, true);
      }

      fields.add(Map.entry(column.name(), fieldType));
    }

    return typeFactory.createStructType(fields);
  }
}
