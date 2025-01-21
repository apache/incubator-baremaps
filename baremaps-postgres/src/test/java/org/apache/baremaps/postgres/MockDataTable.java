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

package org.apache.baremaps.postgres;


import java.util.Iterator;
import java.util.List;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.locationtech.jts.geom.Coordinate;

public class MockDataTable implements DataTable {

  private final DataSchema rowType;

  private final List<DataRow> rows;

  public MockDataTable() {
    this.rowType = new DataSchemaImpl("mock", List.of(
        new DataColumnFixed("string", Cardinality.OPTIONAL, ColumnType.STRING),
        new DataColumnFixed("integer", Cardinality.OPTIONAL, ColumnType.INTEGER),
        new DataColumnFixed("double", Cardinality.OPTIONAL, ColumnType.DOUBLE),
        new DataColumnFixed("float", Cardinality.OPTIONAL, ColumnType.FLOAT),
        new DataColumnFixed("geometry", Cardinality.OPTIONAL, ColumnType.GEOMETRY)));
    this.rows = List.of(
        new DataRowImpl(rowType,
            List.of("string", 1, 1.0, 1.0f,
                Constants.GEOMETRY_FACTORY.createPoint(new Coordinate(1, 1)))),
        new DataRowImpl(rowType,
            List.of("string", 2, 2.0, 2.0f,
                Constants.GEOMETRY_FACTORY.createPoint(new Coordinate(2, 2)))),
        new DataRowImpl(rowType,
            List.of("string", 3, 3.0, 3.0f,
                Constants.GEOMETRY_FACTORY.createPoint(new Coordinate(3, 3)))),
        new DataRowImpl(rowType,
            List.of("string", 4, 4.0, 4.0f,
                Constants.GEOMETRY_FACTORY.createPoint(new Coordinate(4, 4)))),
        new DataRowImpl(rowType,
            List.of("string", 5, 5.0, 5.0f,
                Constants.GEOMETRY_FACTORY.createPoint(new Coordinate(5, 5)))));
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
