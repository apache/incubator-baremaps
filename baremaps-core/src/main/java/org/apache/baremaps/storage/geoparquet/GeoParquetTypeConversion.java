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
import java.util.List;
import org.apache.baremaps.data.storage.DataColumn;
import org.apache.baremaps.data.storage.DataColumn.Type;
import org.apache.baremaps.data.storage.DataColumnImpl;
import org.apache.baremaps.data.storage.DataSchema;
import org.apache.baremaps.data.storage.DataSchemaImpl;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Field;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup.Schema;

public class GeoParquetTypeConversion {

  private GeoParquetTypeConversion() {}

  public static DataSchema asSchema(String table, Schema schema) {
    List<DataColumn> columns = schema.fields().stream()
        .map(field -> (DataColumn) new DataColumnImpl(field.name(), asSchema(field.type())))
        .toList();
    return new DataSchemaImpl(table, columns);
  }

  public static Type asSchema(GeoParquetGroup.Type type) {
    return switch (type) {
      case BINARY -> Type.BYTE_ARRAY;
      case BOOLEAN -> Type.BOOLEAN;
      case INTEGER -> Type.INTEGER;
      case INT96, LONG -> Type.LONG;
      case FLOAT -> Type.FLOAT;
      case DOUBLE -> Type.DOUBLE;
      case STRING -> Type.STRING;
      case GEOMETRY -> Type.GEOMETRY;
      case GROUP -> null;
    };
  }

  public static List<Object> asRowValues(GeoParquetGroup group) {
    List<Object> values = new ArrayList<>();
    Schema schema = group.getSchema();
    List<Field> fields = schema.fields();
    for (int i = 0; i < fields.size(); i++) {
      Field field = fields.get(i);
      field.type();
      switch (field.type()) {
        case BINARY -> values.add(group.getBinaryValue(i).getBytes());
        case BOOLEAN -> values.add(group.getBooleanValue(i));
        case INTEGER -> values.add(group.getIntegerValue(i));
        case INT96, LONG -> values.add(group.getLongValue(i));
        case FLOAT -> values.add(group.getFloatValue(i));
        case DOUBLE -> values.add(group.getDoubleValue(i));
        case STRING -> values.add(group.getStringValue(i));
        case GEOMETRY -> values.add(group.getGeometryValue(i));
        case GROUP -> values.add(null); // TODO: values.add(asDataRow(group.getGroupValue(i)));
      }
    }
    return values;
  }
}
