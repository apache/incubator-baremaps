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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.apache.baremaps.calcite.DataRow;
import org.apache.baremaps.calcite.MockDataTable;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

class PostgresDataTableTest extends PostgresContainerTest {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private PostgresDataStore schema;

  @BeforeEach
  void init() {
    schema = new PostgresDataStore(dataSource());
    schema.add(new MockDataTable());
  }

  @Test
  @Tag("integration")
  void iterator() {
    var table = schema.get("mock");
    var rows = table.stream().toList();
    assertEquals(5, rows.size());
  }

  @Test
  @Tag("integration")
  void sizeAsLong() {
    var table = schema.get("mock");
    assertEquals(5, table.size());
  }

  @Test
  @Tag("integration")
  void schema() {
    var table = schema.get("mock");
    var rowType = table.schema();
    assertNotNull(rowType);
    assertEquals("mock", rowType.name());
    assertEquals(5, rowType.columns().size());
  }

  @Test
  @Tag("integration")
  void add() {
    var table = schema.get("mock");
    var rowType = table.schema();
    var added = table.add(new DataRow(rowType,
        List.of("string", 6, 6.0, 6.0f,
            GEOMETRY_FACTORY.createPoint(new Coordinate(6, 6)))));
    Assertions.assertTrue(added);
    assertEquals(6, table.size());
  }

  @Test
  @Tag("integration")
  void addAll() {
    var table = schema.get("mock");
    var rowType = table.schema();
    var added = table.addAll(List.of(
        new DataRow(rowType,
            List.of("string", 6, 6.0, 6.0f,
                GEOMETRY_FACTORY.createPoint(new Coordinate(6, 6)))),
        new DataRow(rowType,
            List.of("string", 7, 7.0, 7.0f,
                GEOMETRY_FACTORY.createPoint(new Coordinate(7, 7))))));
    Assertions.assertTrue(added);
    assertEquals(7, table.size());
  }
}
