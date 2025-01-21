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

package org.apache.baremaps.postgres.store;

import java.util.EnumMap;
import java.util.Map;
import org.apache.baremaps.store.DataColumn.ColumnType;

@SuppressWarnings("squid:S1192")
public class PostgresTypeConversion {

  private PostgresTypeConversion() {
    // Prevent instantiation
  }

  protected static final Map<ColumnType, String> typeToName = new EnumMap<>(ColumnType.class);

  static {
    typeToName.put(ColumnType.STRING, "varchar");
    typeToName.put(ColumnType.SHORT, "int2");
    typeToName.put(ColumnType.INTEGER, "int4");
    typeToName.put(ColumnType.LONG, "int8");
    typeToName.put(ColumnType.FLOAT, "float4");
    typeToName.put(ColumnType.DOUBLE, "float8");
    typeToName.put(ColumnType.GEOMETRY, "geometry");
    typeToName.put(ColumnType.POINT, "geometry");
    typeToName.put(ColumnType.MULTIPOINT, "geometry");
    typeToName.put(ColumnType.LINESTRING, "geometry");
    typeToName.put(ColumnType.MULTILINESTRING, "geometry");
    typeToName.put(ColumnType.POLYGON, "geometry");
    typeToName.put(ColumnType.MULTIPOLYGON, "geometry");
    typeToName.put(ColumnType.GEOMETRYCOLLECTION, "geometry");
    typeToName.put(ColumnType.ENVELOPE, "geometry");
    typeToName.put(ColumnType.INET_ADDRESS, "inet");
    typeToName.put(ColumnType.INET4_ADDRESS, "inet");
    typeToName.put(ColumnType.INET6_ADDRESS, "inet");
    typeToName.put(ColumnType.LOCAL_DATE, "date");
    typeToName.put(ColumnType.LOCAL_TIME, "time");
    typeToName.put(ColumnType.LOCAL_DATE_TIME, "timestamp");
    typeToName.put(ColumnType.NESTED, "jsonb");
  }

  protected static final Map<String, ColumnType> nameToType = Map.ofEntries(
      Map.entry("varchar", ColumnType.STRING),
      Map.entry("int2", ColumnType.SHORT),
      Map.entry("int4", ColumnType.INTEGER),
      Map.entry("int8", ColumnType.LONG),
      Map.entry("float4", ColumnType.FLOAT),
      Map.entry("float8", ColumnType.DOUBLE),
      Map.entry("geometry", ColumnType.GEOMETRY),
      Map.entry("inet", ColumnType.INET6_ADDRESS),
      Map.entry("date", ColumnType.LOCAL_DATE),
      Map.entry("time", ColumnType.LOCAL_TIME),
      Map.entry("timestamp", ColumnType.LOCAL_DATE_TIME),
      Map.entry("jsonb", ColumnType.NESTED));

}
