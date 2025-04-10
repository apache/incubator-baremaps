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
    POSTGRES_TO_SQL_TYPE.put("json", SqlTypeName.OTHER);
    POSTGRES_TO_SQL_TYPE.put("jsonb", SqlTypeName.OTHER);
    POSTGRES_TO_SQL_TYPE.put("bytea", SqlTypeName.BINARY);
    POSTGRES_TO_SQL_TYPE.put("interval", SqlTypeName.INTERVAL_DAY_SECOND);

    // PostGIS specific types
    POSTGRES_TO_SQL_TYPE.put("geometry", SqlTypeName.GEOMETRY);
    POSTGRES_TO_SQL_TYPE.put("geography", SqlTypeName.GEOMETRY);
    POSTGRES_TO_SQL_TYPE.put("box2d", SqlTypeName.OTHER);
    POSTGRES_TO_SQL_TYPE.put("box3d", SqlTypeName.OTHER);
  }

  /**
   * Converts a PostgreSQL type to a Calcite RelDataType.
   *
   * @param typeFactory the type factory
   * @param postgresType the PostgreSQL type
   * @return the corresponding RelDataType
   */
  public static RelDataType postgresTypeToRelDataType(
      RelDataTypeFactory typeFactory, String postgresType) {
    switch (postgresType.toLowerCase()) {
      case "int4":
      case "integer":
        return typeFactory.createSqlType(SqlTypeName.INTEGER);
      case "bigint":
      case "int8":
        return typeFactory.createSqlType(SqlTypeName.BIGINT);
      case "smallint":
      case "int2":
        return typeFactory.createSqlType(SqlTypeName.SMALLINT);
      case "real":
      case "float4":
        return typeFactory.createSqlType(SqlTypeName.FLOAT);
      case "double precision":
      case "float8":
        return typeFactory.createSqlType(SqlTypeName.DOUBLE);
      case "numeric":
      case "decimal":
        return typeFactory.createSqlType(SqlTypeName.DECIMAL);
      case "boolean":
      case "bool":
        return typeFactory.createSqlType(SqlTypeName.BOOLEAN);
      case "varchar":
      case "character varying":
      case "text":
        return typeFactory.createSqlType(SqlTypeName.VARCHAR);
      case "char":
      case "character":
        return typeFactory.createSqlType(SqlTypeName.CHAR);
      case "date":
        return typeFactory.createSqlType(SqlTypeName.DATE);
      case "timestamp":
      case "timestamp without time zone":
        return typeFactory.createSqlType(SqlTypeName.TIMESTAMP);
      case "timestamp with time zone":
        return typeFactory.createSqlType(SqlTypeName.TIMESTAMP_WITH_LOCAL_TIME_ZONE);
      case "time":
      case "time without time zone":
        return typeFactory.createSqlType(SqlTypeName.TIME);
      case "time with time zone":
        return typeFactory.createSqlType(SqlTypeName.TIME_WITH_LOCAL_TIME_ZONE);
      case "bytea":
        return typeFactory.createSqlType(SqlTypeName.BINARY);
      case "geometry":
        return typeFactory.createSqlType(SqlTypeName.GEOMETRY);
      case "json":
      case "jsonb":
        return typeFactory.createSqlType(SqlTypeName.OTHER);
      default:
        // Default to VARCHAR for unknown types
        return typeFactory.createSqlType(SqlTypeName.VARCHAR);
    }
  }

  /**
   * Converts a RelDataType to a PostgreSQL type string.
   *
   * @param type the RelDataType
   * @return the corresponding PostgreSQL type string
   */
  public static String toPostgresTypeString(RelDataType type) {
    // Handle record types (structs)
    if (type.isStruct()) {
      // For struct types, use JSONB in PostgreSQL
      return "JSONB";
    }

    SqlTypeName typeName = type.getSqlTypeName();
    switch (typeName) {
      case INTEGER:
        return "INTEGER";
      case BIGINT:
        return "BIGINT";
      case SMALLINT:
        return "SMALLINT";
      case TINYINT:
        return "SMALLINT";
      case FLOAT:
        return "REAL";
      case DOUBLE:
      case DECIMAL:
        return "DOUBLE PRECISION";
      case BOOLEAN:
        return "BOOLEAN";
      case VARCHAR:
      case CHAR:
        int precision = type.getPrecision();
        if (precision == RelDataType.PRECISION_NOT_SPECIFIED) {
          return "TEXT";
        } else {
          return typeName == SqlTypeName.VARCHAR
              ? "VARCHAR(" + precision + ")"
              : "CHAR(" + precision + ")";
        }
      case DATE:
        return "DATE";
      case TIMESTAMP:
        return "TIMESTAMP";
      case TIMESTAMP_WITH_LOCAL_TIME_ZONE:
        return "TIMESTAMP WITH TIME ZONE";
      case TIME:
        return "TIME";
      case TIME_WITH_LOCAL_TIME_ZONE:
        return "TIME WITH LOCAL TIME ZONE";
      case BINARY:
      case VARBINARY:
        return "BYTEA";
      case GEOMETRY:
        return "GEOMETRY";
      case ARRAY:
        return toPostgresTypeString(type.getComponentType()) + "[]";
      case OTHER:
        // For JSON/JSONB and other types
        return "JSONB";
      default:
        // Default to TEXT for unknown types
        return "TEXT";
    }
  }

  /**
   * Converts a DataSchema to a RelDataType.
   *
   * @param typeFactory the type factory
   * @param schema the data schema
   * @return the corresponding RelDataType
   */
  public static RelDataType toRelDataType(RelDataTypeFactory typeFactory,
      DataSchema schema) {
    List<RelDataType> types = new ArrayList<>();
    List<String> names = new ArrayList<>();

    for (DataColumn column : schema.columns()) {
      names.add(column.name());
      types.add(column.relDataType());
    }

    return typeFactory.createStructType(types, names);
  }
}
