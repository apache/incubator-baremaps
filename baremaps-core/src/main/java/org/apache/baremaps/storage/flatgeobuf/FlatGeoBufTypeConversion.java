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

package org.apache.baremaps.storage.flatgeobuf;


import java.util.*;
import org.apache.baremaps.data.storage.*;
import org.apache.baremaps.data.storage.DataColumn.Cardinality;
import org.apache.baremaps.data.storage.DataColumn.Type;
import org.apache.baremaps.flatgeobuf.FlatGeoBuf;
import org.apache.baremaps.flatgeobuf.FlatGeoBuf.Feature;
import org.apache.baremaps.flatgeobuf.generated.ColumnType;
import org.locationtech.jts.geom.Geometry;

public class FlatGeoBufTypeConversion {

  private static final Map<Type, Integer> types = new EnumMap<>(Type.class);

  static {
    types.put(Type.BYTE, ColumnType.Byte);
    types.put(Type.BOOLEAN, ColumnType.Bool);
    types.put(Type.SHORT, ColumnType.Short);
    types.put(Type.INTEGER, ColumnType.Int);
    types.put(Type.LONG, ColumnType.Long);
    types.put(Type.FLOAT, ColumnType.Float);
    types.put(Type.DOUBLE, ColumnType.Double);
    types.put(Type.STRING, ColumnType.String);
  }

  private FlatGeoBufTypeConversion() {
    // Prevent instantiation
  }

  public static DataSchema asSchema(FlatGeoBuf.Header header) {
    var name = header.name();
    var columns = header.columns().stream()
        .map(column -> new DataColumnFixed(
            column.name(),
            column.nullable() ? Cardinality.OPTIONAL : Cardinality.REQUIRED,
            Type.fromBinding(fromColumnType(column.type()))))
        .map(DataColumn.class::cast)
        .toList();
    return new DataSchemaImpl(name, columns);
  }

  private static Class<?> fromColumnType(FlatGeoBuf.ColumnType columnType) {
    return switch (columnType) {
      case BYTE -> Byte.class;
      case UBYTE -> Byte.class;
      case BOOL -> Boolean.class;
      case SHORT -> Short.class;
      case USHORT -> Short.class;
      case INT -> Integer.class;
      case UINT -> Integer.class;
      case LONG -> Long.class;
      case ULONG -> Long.class;
      case FLOAT -> Float.class;
      case DOUBLE -> Double.class;
      case STRING -> String.class;
      case JSON -> throw new UnsupportedOperationException();
      case DATETIME -> throw new UnsupportedOperationException();
      case BINARY -> throw new UnsupportedOperationException();
    };
  }

  public static DataRow asRow(DataSchema dataType, Feature feature) {
    var values = new ArrayList<>();

    var geometry = feature.geometry();
    values.add(geometry);

    if (!feature.properties().isEmpty()) {
      values.addAll(feature.properties());
    }

    return new DataRowImpl(dataType, values);
  }

  public static List<FlatGeoBuf.Column> asColumns(List<DataColumn> columns) {
    return columns.stream()
        .map(FlatGeoBufTypeConversion::asColumn)
        .filter(Objects::nonNull)
        .toList();
  }

  public static FlatGeoBuf.Column asColumn(DataColumn column) {
    var type = types.get(column.type());
    if (type == null) {
      return null;
    }
    return new FlatGeoBuf.Column(
        column.name(),
        FlatGeoBuf.ColumnType.values()[type],
        null,
        null,
        0,
        0,
        0,
        column.cardinality() == Cardinality.OPTIONAL,
        false,
        false,
        null);
  }

  public static FlatGeoBuf.Feature asFeature(DataRow row) {
    var geometry = row.values().stream()
        .filter(v -> v instanceof Geometry)
        .map(Geometry.class::cast)
        .findFirst()
        .orElse(null);
    var properties = row.values().stream()
        .filter(v -> !(v instanceof Geometry))
        .toList();
    return new FlatGeoBuf.Feature(properties, geometry);
  }
}
