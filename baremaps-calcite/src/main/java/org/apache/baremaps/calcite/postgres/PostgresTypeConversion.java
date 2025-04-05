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

import java.util.EnumMap;
import java.util.Map;
import org.apache.baremaps.calcite.DataColumn.Type;

@SuppressWarnings("squid:S1192")
public class PostgresTypeConversion {

  private PostgresTypeConversion() {
    // Prevent instantiation
  }

  protected static final Map<Type, String> typeToName = new EnumMap<>(Type.class);

  static {
    typeToName.put(Type.STRING, "varchar");
    typeToName.put(Type.SHORT, "int2");
    typeToName.put(Type.INTEGER, "int4");
    typeToName.put(Type.LONG, "int8");
    typeToName.put(Type.FLOAT, "float4");
    typeToName.put(Type.DOUBLE, "float8");
    typeToName.put(Type.GEOMETRY, "geometry");
    typeToName.put(Type.POINT, "geometry");
    typeToName.put(Type.MULTIPOINT, "geometry");
    typeToName.put(Type.LINESTRING, "geometry");
    typeToName.put(Type.MULTILINESTRING, "geometry");
    typeToName.put(Type.POLYGON, "geometry");
    typeToName.put(Type.MULTIPOLYGON, "geometry");
    typeToName.put(Type.GEOMETRYCOLLECTION, "geometry");
    typeToName.put(Type.ENVELOPE, "geometry");
    typeToName.put(Type.INET_ADDRESS, "inet");
    typeToName.put(Type.INET4_ADDRESS, "inet");
    typeToName.put(Type.INET6_ADDRESS, "inet");
    typeToName.put(Type.LOCAL_DATE, "date");
    typeToName.put(Type.LOCAL_TIME, "time");
    typeToName.put(Type.LOCAL_DATE_TIME, "timestamp");
    typeToName.put(Type.NESTED, "jsonb");
  }

  protected static final Map<String, Type> nameToType = Map.ofEntries(
      Map.entry("varchar", Type.STRING),
      Map.entry("int2", Type.SHORT),
      Map.entry("int4", Type.INTEGER),
      Map.entry("int8", Type.LONG),
      Map.entry("float4", Type.FLOAT),
      Map.entry("float8", Type.DOUBLE),
      Map.entry("geometry", Type.GEOMETRY),
      Map.entry("inet", Type.INET6_ADDRESS),
      Map.entry("date", Type.LOCAL_DATE),
      Map.entry("time", Type.LOCAL_TIME),
      Map.entry("timestamp", Type.LOCAL_DATE_TIME),
      Map.entry("jsonb", Type.NESTED));

}
