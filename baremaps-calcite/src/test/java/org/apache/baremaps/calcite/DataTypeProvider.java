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

package org.apache.baremaps.calcite;


import java.util.List;
import java.util.stream.Stream;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.junit.jupiter.params.provider.Arguments;
import org.locationtech.jts.geom.*;

public class DataTypeProvider {

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  private static final DataSchema DATA_SCHEMA = new DataSchema("row", List.of(
      new DataColumnFixed("byte", Cardinality.OPTIONAL, Type.BYTE),
      new DataColumnFixed("boolean", Cardinality.OPTIONAL, Type.BOOLEAN),
      new DataColumnFixed("short", Cardinality.OPTIONAL, Type.SHORT),
      new DataColumnFixed("integer", Cardinality.OPTIONAL, Type.INTEGER),
      new DataColumnFixed("long", Cardinality.OPTIONAL, Type.LONG),
      new DataColumnFixed("float", Cardinality.OPTIONAL, Type.FLOAT),
      new DataColumnFixed("double", Cardinality.OPTIONAL, Type.DOUBLE),
      new DataColumnFixed("string", Cardinality.OPTIONAL, Type.STRING),
      new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY),
      new DataColumnFixed("point", Cardinality.OPTIONAL, Type.POINT),
      new DataColumnFixed("linestring", Cardinality.OPTIONAL, Type.LINESTRING),
      new DataColumnFixed("polygon", Cardinality.OPTIONAL, Type.POLYGON),
      new DataColumnFixed("multipoint", Cardinality.OPTIONAL, Type.MULTIPOINT),
      new DataColumnFixed("multilinestring", Cardinality.OPTIONAL, Type.MULTILINESTRING),
      new DataColumnFixed("multipolygon", Cardinality.OPTIONAL, Type.MULTIPOLYGON),
      new DataColumnFixed("geometrycollection", Cardinality.OPTIONAL,
          Type.GEOMETRYCOLLECTION),
      new DataColumnFixed("coordinate", Cardinality.OPTIONAL, Type.COORDINATE)));

  private static final DataRow DATA_ROW = DATA_SCHEMA.createRow()
      .with("byte", Byte.MAX_VALUE)
      .with("boolean", true)
      .with("short", Short.MAX_VALUE)
      .with("integer", Integer.MAX_VALUE)
      .with("long", Long.MAX_VALUE)
      .with("float", Float.MAX_VALUE)
      .with("double", Double.MAX_VALUE)
      .with("string", "Hello, World!")
      .with("geometry", geometryFactory.createPoint(new Coordinate(0, 0)))
      .with("point", geometryFactory.createPoint(new Coordinate(0, 0)))
      .with("linestring",
          geometryFactory
              .createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)}))
      .with("polygon",
          geometryFactory.createPolygon(new Coordinate[] {new Coordinate(0, 0),
              new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)}))
      .with("multipoint",
          geometryFactory
              .createMultiPoint(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)}))
      .with("multilinestring",
          geometryFactory.createMultiLineString(new LineString[] {geometryFactory
              .createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)})}))
      .with("multipolygon",
          geometryFactory.createMultiPolygon(
              new Polygon[] {geometryFactory.createPolygon(new Coordinate[] {new Coordinate(0, 0),
                  new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)})}))
      .with("geometrycollection",
          geometryFactory.createGeometryCollection(
              new Geometry[] {geometryFactory.createPoint(new Coordinate(0, 0))}))
      .with("coordinate", new Coordinate(0, 0));

  private static Stream<Arguments> dataTypes() {
    return Stream.of(Arguments.of(new DataRowType(DATA_SCHEMA), DATA_ROW));


  }
}
