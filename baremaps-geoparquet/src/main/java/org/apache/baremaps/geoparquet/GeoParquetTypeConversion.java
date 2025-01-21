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

package org.apache.baremaps.geoparquet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.geoparquet.GeoParquetSchema.Field;
import org.apache.baremaps.geoparquet.GeoParquetSchema.GroupField;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.apache.parquet.io.api.Binary;

public class GeoParquetTypeConversion {

  private GeoParquetTypeConversion() {}

  public static DataSchema asSchema(String table, GeoParquetSchema schema) {
    List<DataColumn> columns = asDataColumns(schema);
    return new DataSchemaImpl(table, columns);
  }

  private static List<DataColumn> asDataColumns(GeoParquetSchema field) {
    return field.fields().stream()
        .map(GeoParquetTypeConversion::asDataColumn)
        .toList();
  }

  private static DataColumn asDataColumn(Field field) {
    Cardinality cardinality = switch (field.cardinality()) {
      case REQUIRED -> Cardinality.REQUIRED;
      case OPTIONAL -> Cardinality.OPTIONAL;
      case REPEATED -> Cardinality.REPEATED;
    };
    return switch (field.type()) {
      case BINARY -> new DataColumnFixed(field.name(), cardinality, ColumnType.BINARY);
      case BOOLEAN -> new DataColumnFixed(field.name(), cardinality, ColumnType.BOOLEAN);
      case INTEGER -> new DataColumnFixed(field.name(), cardinality, ColumnType.INTEGER);
      case INT96, LONG -> new DataColumnFixed(field.name(), cardinality, ColumnType.LONG);
      case FLOAT -> new DataColumnFixed(field.name(), cardinality, ColumnType.FLOAT);
      case DOUBLE -> new DataColumnFixed(field.name(), cardinality, ColumnType.DOUBLE);
      case STRING -> new DataColumnFixed(field.name(), cardinality, ColumnType.STRING);
      case GEOMETRY -> new DataColumnFixed(field.name(), cardinality, ColumnType.GEOMETRY);
      case ENVELOPE -> new DataColumnFixed(field.name(), cardinality, ColumnType.ENVELOPE);
      case GROUP -> new DataColumnNested(field.name(), cardinality,
          asDataColumns(((GroupField) field).schema()));
    };
  }

  public static List<Object> asRowValues(GeoParquetGroup group) {
    GeoParquetSchema schema = group.getGeoParquetSchema();
    List<Field> fields = schema.fields();
    List<Object> values = new ArrayList<>();
    for (int i = 0; i < fields.size(); i++) {
      Field field = fields.get(i);
      values.add(asValue(field, group, i));
    }
    return values;
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
      Object value = asValue(field, group, i);
      nested.put(field.name(), value);
    }
    return nested;
  }

  public static Object asValue(Field field, GeoParquetGroup group, int i) {
    if (field.cardinality() == GeoParquetSchema.Cardinality.REPEATED) {
      return switch (field.type()) {
        case BINARY -> group.getBinaryValues(i).stream().map(Binary::getBytes).toList();
        case BOOLEAN -> group.getBooleanValues(i);
        case INTEGER -> group.getIntegerValues(i);
        case INT96, LONG -> group.getLongValues(i);
        case FLOAT -> group.getFloatValues(i);
        case DOUBLE -> group.getDoubleValues(i);
        case STRING -> group.getStringValues(i);
        case GEOMETRY -> group.getGeometryValues(i);
        case ENVELOPE -> group.getEnvelopeValues(i);
        case GROUP -> group.getGroupValues(i).stream().map(GeoParquetTypeConversion::asNested)
            .toList();
      };
    } else {
      return switch (field.type()) {
        case BINARY -> group.getBinaryValue(i).getBytes();
        case BOOLEAN -> group.getBooleanValue(i);
        case INTEGER -> group.getIntegerValue(i);
        case INT96, LONG -> group.getLongValue(i);
        case FLOAT -> group.getFloatValue(i);
        case DOUBLE -> group.getDoubleValue(i);
        case STRING -> group.getStringValue(i);
        case GEOMETRY -> group.getGeometryValue(i);
        case ENVELOPE -> group.getEnvelopeValue(i);
        case GROUP -> asNested(group.getGroupValue(i));
      };
    }
  }
}
