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

package org.apache.baremaps.calcite.geoparquet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.baremaps.geoparquet.GeoParquetGroup;
import org.apache.baremaps.geoparquet.GeoParquetReader;
import org.apache.baremaps.geoparquet.GeoParquetSchema;
import org.apache.baremaps.geoparquet.GeoParquetSchema.Cardinality;
import org.apache.baremaps.geoparquet.GeoParquetSchema.Field;
import org.apache.baremaps.geoparquet.GeoParquetSchema.GroupField;
import org.apache.baremaps.geoparquet.GeoParquetSchema.Type;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.PrimitiveType;
import org.locationtech.jts.geom.Geometry;

public class GeoParquetTypeConversion {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private GeoParquetTypeConversion() {}

  public static RelDataType toRelDataType(RelDataTypeFactory typeFactory, GeoParquetReader reader) {
    return toRelDataType(typeFactory, reader.getGeoParquetSchema());
  }

  public static RelDataType toRelDataType(RelDataTypeFactory typeFactory, GeoParquetSchema schema) {
    List<RelDataType> types = new ArrayList<>();
    List<String> names = new ArrayList<>();

    for (Field field : schema.fields()) {
      types.add(toRelDataTypeFromField(typeFactory, field));
      names.add(field.name());
    }

    return typeFactory.createStructType(types, names);
  }

  private static RelDataType toRelDataTypeFromField(RelDataTypeFactory typeFactory, Field field) {
    Type type = field.type();

    if (type == Type.BINARY) {
      return typeFactory.createSqlType(SqlTypeName.VARBINARY);
    } else if (type == Type.BOOLEAN) {
      return typeFactory.createSqlType(SqlTypeName.BOOLEAN);
    } else if (type == Type.INTEGER) {
      return typeFactory.createSqlType(SqlTypeName.INTEGER);
    } else if (type == Type.FLOAT) {
      return typeFactory.createSqlType(SqlTypeName.FLOAT);
    } else if (type == Type.DOUBLE) {
      return typeFactory.createSqlType(SqlTypeName.DOUBLE);
    } else if (type == Type.STRING) {
      return typeFactory.createSqlType(SqlTypeName.VARCHAR);
    } else if (type == Type.GEOMETRY) {
      return typeFactory.createJavaType(Geometry.class);
    } else if (type == Type.ENVELOPE) {
      return typeFactory.createStructType(
          List.of(
              typeFactory.createSqlType(SqlTypeName.DOUBLE),
              typeFactory.createSqlType(SqlTypeName.DOUBLE),
              typeFactory.createSqlType(SqlTypeName.DOUBLE),
              typeFactory.createSqlType(SqlTypeName.DOUBLE)),
          List.of("minx", "miny", "maxx", "maxy"));
    } else if (type == Type.LONG || type == Type.INT96) {
      return typeFactory.createSqlType(SqlTypeName.BIGINT);
    } else if (type == Type.GROUP) {
      var groupField = (GroupField) field;
      var fields = groupField.schema().fields();
      var types = fields.stream()
          .map(f -> toRelDataTypeFromField(typeFactory, f))
          .collect(Collectors.toList());
      var names = fields.stream()
          .map(Field::name)
          .collect(Collectors.toList());
      return typeFactory.createStructType(types, names);
    } else {
      throw new IllegalStateException("Unexpected value: " + type);
    }
  }

  private static RelDataType toRelDataType(RelDataTypeFactory typeFactory,
      org.apache.parquet.schema.Type field) {
    if (field.isPrimitive()) {
      PrimitiveType primitiveType = field.asPrimitiveType();
      PrimitiveType.PrimitiveTypeName typeName = primitiveType.getPrimitiveTypeName();

      if (typeName == PrimitiveType.PrimitiveTypeName.BOOLEAN) {
        return typeFactory.createSqlType(SqlTypeName.BOOLEAN);
      } else if (typeName == PrimitiveType.PrimitiveTypeName.INT32) {
        return typeFactory.createSqlType(SqlTypeName.INTEGER);
      } else if (typeName == PrimitiveType.PrimitiveTypeName.INT64) {
        return typeFactory.createSqlType(SqlTypeName.BIGINT);
      } else if (typeName == PrimitiveType.PrimitiveTypeName.FLOAT) {
        return typeFactory.createSqlType(SqlTypeName.FLOAT);
      } else if (typeName == PrimitiveType.PrimitiveTypeName.DOUBLE) {
        return typeFactory.createSqlType(SqlTypeName.DOUBLE);
      } else if (typeName == PrimitiveType.PrimitiveTypeName.BINARY) {
        if (field.getName().equals("geometry")) {
          return typeFactory.createJavaType(Geometry.class);
        }
        return typeFactory.createSqlType(SqlTypeName.VARBINARY);
      } else if (typeName == PrimitiveType.PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY) {
        return typeFactory.createSqlType(SqlTypeName.VARBINARY);
      } else if (typeName == PrimitiveType.PrimitiveTypeName.INT96) {
        return typeFactory.createSqlType(SqlTypeName.VARBINARY);
      } else {
        throw new IllegalArgumentException(
            "Unsupported Parquet type: " + typeName);
      }
    } else {
      // For group types, we'll create a struct type
      return typeFactory.createStructType(
          field.asGroupType().getFields().stream()
              .map(f -> toRelDataType(typeFactory, f))
              .toList(),
          field.asGroupType().getFields().stream()
              .map(org.apache.parquet.schema.Type::getName)
              .toList());
    }
  }

  public static List<Object> asRowValues(GeoParquetGroup group) {
    List<Object> values = new ArrayList<>();
    GeoParquetSchema schema = group.getGeoParquetSchema();

    for (int i = 0; i < schema.fields().size(); i++) {
      Field field = schema.fields().get(i);
      values.add(convertValue(field, group, i));
    }

    return values;
  }

  public static Object convertValue(Field field, GeoParquetGroup group, int index) {
    // Handle repeated fields
    if (field.cardinality() == Cardinality.REPEATED) {
      // Use if-else instead of switch expression to avoid enum constant issues
      Type type = field.type();
      if (type == Type.BINARY) {
        return group.getBinaryValues(index).stream().map(Binary::getBytes).toList();
      } else if (type == Type.BOOLEAN) {
        return group.getBooleanValues(index);
      } else if (type == Type.INTEGER) {
        return group.getIntegerValues(index);
      } else if (type == Type.INT96 || type == Type.LONG) {
        return group.getLongValues(index);
      } else if (type == Type.FLOAT) {
        return group.getFloatValues(index);
      } else if (type == Type.DOUBLE) {
        return group.getDoubleValues(index);
      } else if (type == Type.STRING) {
        return group.getStringValues(index);
      } else if (type == Type.GEOMETRY) {
        return group.getGeometryValues(index);
      } else if (type == Type.ENVELOPE) {
        return group.getEnvelopeValues(index);
      } else if (type == Type.GROUP) {
        return group.getGroupValues(index).stream().map(GeoParquetTypeConversion::asNested)
            .toList();
      } else {
        throw new IllegalArgumentException("Unsupported type: " + type);
      }
    } else {
      // Handle non-repeated fields
      // Use if-else instead of switch expression to avoid enum constant issues
      Type type = field.type();
      if (type == Type.BINARY) {
        return group.getBinaryValue(index).getBytes();
      } else if (type == Type.BOOLEAN) {
        return group.getBooleanValue(index);
      } else if (type == Type.INTEGER) {
        return group.getIntegerValue(index);
      } else if (type == Type.INT96 || type == Type.LONG) {
        return group.getLongValue(index);
      } else if (type == Type.FLOAT) {
        return group.getFloatValue(index);
      } else if (type == Type.DOUBLE) {
        return group.getDoubleValue(index);
      } else if (type == Type.STRING) {
        return group.getStringValue(index);
      } else if (type == Type.GEOMETRY) {
        return group.getGeometryValue(index);
      } else if (type == Type.ENVELOPE) {
        return group.getEnvelopeValue(index);
      } else if (type == Type.GROUP) {
        return asNested(group.getGroupValue(index));
      } else {
        throw new IllegalArgumentException("Unsupported type: " + type);
      }
    }
  }

  public static Map<String, Object> asNested(GeoParquetGroup group) {
    Map<String, Object> nested = new HashMap<>();
    GeoParquetSchema schema = group.getGeoParquetSchema();
    List<Field> fields = schema.fields();

    for (int i = 0; i < fields.size(); i++) {
      if (group.getValues(i).isEmpty()) {
        continue;
      }
      Field field = fields.get(i);
      Object value = convertValue(field, group, i);
      nested.put(field.name(), value);
    }

    return nested;
  }

  /**
   * Converts a GeoParquetGroup to a list of values suitable for PostgreSQL. This method handles
   * record types by converting them to JSON strings.
   *
   * @param group the GeoParquetGroup to convert
   * @return a list of values suitable for PostgreSQL
   */
  public static List<Object> asPostgresRowValues(GeoParquetGroup group) {
    List<Object> values = new ArrayList<>();
    GeoParquetSchema schema = group.getGeoParquetSchema();

    for (int i = 0; i < schema.fields().size(); i++) {
      Field field = schema.fields().get(i);
      Object value = convertValue(field, group, i);

      // Convert record types to JSON strings
      if (field.type() == Type.GROUP || field.type() == Type.ENVELOPE) {
        try {
          if (field.type() == Type.GROUP) {
            value = MAPPER.writeValueAsString(value);
          } else if (field.type() == Type.ENVELOPE) {
            // Convert envelope to a map with minx, miny, maxx, maxy
            Map<String, Object> envelopeMap = new HashMap<>();
            if (value instanceof org.locationtech.jts.geom.Envelope) {
              org.locationtech.jts.geom.Envelope envelope =
                  (org.locationtech.jts.geom.Envelope) value;
              envelopeMap.put("minx", envelope.getMinX());
              envelopeMap.put("miny", envelope.getMinY());
              envelopeMap.put("maxx", envelope.getMaxX());
              envelopeMap.put("maxy", envelope.getMaxY());
            } else if (value instanceof Object[]) {
              Object[] envelope = (Object[]) value;
              envelopeMap.put("minx", envelope[0]);
              envelopeMap.put("miny", envelope[1]);
              envelopeMap.put("maxx", envelope[2]);
              envelopeMap.put("maxy", envelope[3]);
            } else {
              throw new IllegalArgumentException(
                  "Unexpected envelope type: " + value.getClass().getName());
            }
            value = MAPPER.writeValueAsString(envelopeMap);
          }
        } catch (Exception e) {
          throw new RuntimeException("Error converting record type to JSON", e);
        }
      }

      values.add(value);
    }

    return values;
  }
}
