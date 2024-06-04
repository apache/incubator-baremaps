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

package org.apache.baremaps.storage.geoparquet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.data.storage.*;
import org.apache.baremaps.data.storage.DataColumn.Cardinality;
import org.apache.baremaps.data.storage.DataColumn.Type;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Field;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.GroupField;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Schema;

public class GeoParquetTypeConversion {

  private GeoParquetTypeConversion() {}

  public static DataSchema asSchema(String table, Schema schema) {
    List<DataColumn> columns = asDataColumns(schema);
    return new DataSchemaImpl(table, columns);
  }

  private static List<DataColumn> asDataColumns(Schema field) {
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

  public static List<Object> asRowValues(GeoParquetGroup group) {
    List<Object> values = new ArrayList<>();
    Schema schema = group.getSchema();
    List<Field> fields = schema.fields();
    for (int i = 0; i < fields.size(); i++) {
      if (group.getValues(i).isEmpty()) {
        values.add(null);
        continue;
      }
      Field field = fields.get(i);
      switch (field.type()) {
        case BINARY -> values.add(group.getBinaryValue(i).getBytes());
        case BOOLEAN -> values.add(group.getBooleanValue(i));
        case INTEGER -> values.add(group.getIntegerValue(i));
        case INT96, LONG -> values.add(group.getLongValue(i));
        case FLOAT -> values.add(group.getFloatValue(i));
        case DOUBLE -> values.add(group.getDoubleValue(i));
        case STRING -> values.add(group.getStringValue(i));
        case GEOMETRY -> values.add(group.getGeometryValue(i));
        case ENVELOPE -> values.add(group.getEnvelopeValue(i));
        case GROUP -> values.add(asNested(group.getGroupValue(i)));
      }
    }
    return values;
  }

  public static Map<String, Object> asNested(GeoParquetGroup group) {
    Map<String, Object> nested = new HashMap<>();
    Schema schema = group.getSchema();
    List<Field> fields = schema.fields();
    for (int i = 0; i < fields.size(); i++) {
      if (group.getValues(i).isEmpty()) {
        continue;
      }
      Field field = fields.get(i);
      nested.put(field.name(), switch (field.type()) {
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
      });
    }
    return nested;
  }

}
