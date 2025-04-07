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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.calcite.*;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.geoparquet.GeoParquetGroup;
import org.apache.baremaps.geoparquet.GeoParquetSchema;
import org.apache.baremaps.geoparquet.GeoParquetSchema.Field;
import org.apache.baremaps.geoparquet.GeoParquetSchema.GroupField;
import org.apache.parquet.io.api.Binary;

/**
 * Utility class for converting between GeoParquet types and Baremaps data types.
 */
public class GeoParquetTypeConversion {

  private GeoParquetTypeConversion() {}

  /**
   * Converts a GeoParquet schema to a Baremaps DataSchema.
   *
   * @param table the table name
   * @param schema the GeoParquet schema
   * @return the Baremaps DataSchema
   */
  public static DataSchema asSchema(String table, GeoParquetSchema schema) {
    List<DataColumn> columns = asDataColumns(schema);
    return new DataSchema(table, columns);
  }

  /**
   * Converts a GeoParquet schema to a list of Baremaps DataColumns.
   *
   * @param field the GeoParquet schema
   * @return the list of Baremaps DataColumns
   */
  private static List<DataColumn> asDataColumns(GeoParquetSchema field) {
    return field.fields().stream()
        .map(GeoParquetTypeConversion::asDataColumn)
        .toList();
  }

  /**
   * Converts a GeoParquet field to a Baremaps DataColumn.
   *
   * @param field the GeoParquet field
   * @return the Baremaps DataColumn
   */
  private static DataColumn asDataColumn(Field field) {
    Cardinality cardinality = switch (field.cardinality()) {
      case REQUIRED -> Cardinality.REQUIRED;
      case OPTIONAL -> Cardinality.OPTIONAL;
      case REPEATED -> Cardinality.REPEATED;
    };

    return switch (field.type()) {
      case BINARY -> new DataColumnFixed(field.name(), cardinality, Type.BINARY);
      case BOOLEAN -> new DataColumnFixed(field.name(), cardinality, Type.BOOLEAN);
      case INTEGER -> new DataColumnFixed(field.name(), cardinality, Type.INTEGER);
      case INT96, LONG -> new DataColumnFixed(field.name(), cardinality, Type.LONG);
      case FLOAT -> new DataColumnFixed(field.name(), cardinality, Type.FLOAT);
      case DOUBLE -> new DataColumnFixed(field.name(), cardinality, Type.DOUBLE);
      case STRING -> new DataColumnFixed(field.name(), cardinality, Type.STRING);
      case GEOMETRY -> new DataColumnFixed(field.name(), cardinality, Type.GEOMETRY);
      case ENVELOPE -> new DataColumnFixed(field.name(), cardinality, Type.ENVELOPE);
      case GROUP -> new DataColumnNested(field.name(), cardinality,
          asDataColumns(((GroupField) field).schema()));
    };
  }

  /**
   * Converts a GeoParquet group to a list of row values.
   *
   * @param group the GeoParquet group
   * @return the list of row values
   */
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

  /**
   * Converts a GeoParquet group to a nested map.
   *
   * @param group the GeoParquet group
   * @return the nested map
   */
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

  /**
   * Converts a GeoParquet field value to a Java object.
   *
   * @param field the GeoParquet field
   * @param group the GeoParquet group
   * @param i the field index
   * @return the Java object
   */
  public static Object asValue(Field field, GeoParquetGroup group, int i) {
    // Handle repeated fields
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
      // Handle non-repeated fields
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
