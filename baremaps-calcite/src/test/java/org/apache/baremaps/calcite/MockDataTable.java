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


import java.util.Iterator;
import java.util.List;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class MockDataTable implements DataTable {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private final DataSchema rowType;

  private final List<DataRow> rows;

  public MockDataTable() {
    this.rowType = new DataSchema("mock", List.of(
        new DataColumnFixed("string", Cardinality.OPTIONAL, Type.STRING),
        new DataColumnFixed("integer", Cardinality.OPTIONAL, Type.INTEGER),
        new DataColumnFixed("double", Cardinality.OPTIONAL, Type.DOUBLE),
        new DataColumnFixed("float", Cardinality.OPTIONAL, Type.FLOAT),
        new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY)));
    this.rows = List.of(
        new DataRow(rowType,
            List.of("string", 1, 1.0, 1.0f,
                GEOMETRY_FACTORY.createPoint(new Coordinate(1, 1)))),
        new DataRow(rowType,
            List.of("string", 2, 2.0, 2.0f,
                GEOMETRY_FACTORY.createPoint(new Coordinate(2, 2)))),
        new DataRow(rowType,
            List.of("string", 3, 3.0, 3.0f,
                GEOMETRY_FACTORY.createPoint(new Coordinate(3, 3)))),
        new DataRow(rowType,
            List.of("string", 4, 4.0, 4.0f,
                GEOMETRY_FACTORY.createPoint(new Coordinate(4, 4)))),
        new DataRow(rowType,
            List.of("string", 5, 5.0, 5.0f,
                GEOMETRY_FACTORY.createPoint(new Coordinate(5, 5)))));
  }

  @Override
  public long size() {
    return rows.size();
  }

  @Override
  public Iterator<DataRow> iterator() {
    return rows.iterator();
  }

  @Override
  public void clear() {
    rows.clear();
  }

  @Override
  public DataSchema schema() {
    return rowType;
  }

  @Override
  public void close() throws Exception {
    // Do nothing
  }
}
