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

package org.apache.baremaps.flatgeobuf;


import java.util.*;
import org.apache.baremaps.flatgeobuf.FlatGeoBuf.Feature;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.locationtech.jts.geom.Geometry;

public class FlatGeoBufTypeConversion {

  private static final Map<ColumnType, Integer> types = new EnumMap<>(ColumnType.class);

  static {
    types.put(ColumnType.BYTE, org.apache.baremaps.flatgeobuf.generated.ColumnType.Byte);
    types.put(ColumnType.BOOLEAN, org.apache.baremaps.flatgeobuf.generated.ColumnType.Bool);
    types.put(ColumnType.SHORT, org.apache.baremaps.flatgeobuf.generated.ColumnType.Short);
    types.put(ColumnType.INTEGER, org.apache.baremaps.flatgeobuf.generated.ColumnType.Int);
    types.put(ColumnType.LONG, org.apache.baremaps.flatgeobuf.generated.ColumnType.Long);
    types.put(ColumnType.FLOAT, org.apache.baremaps.flatgeobuf.generated.ColumnType.Float);
    types.put(ColumnType.DOUBLE, org.apache.baremaps.flatgeobuf.generated.ColumnType.Double);
    types.put(ColumnType.STRING, org.apache.baremaps.flatgeobuf.generated.ColumnType.String);
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
            ColumnType.fromBinding(fromColumnType(column.type()))))
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
