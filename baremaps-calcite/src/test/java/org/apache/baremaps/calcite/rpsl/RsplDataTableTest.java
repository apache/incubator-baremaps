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

package org.apache.baremaps.calcite.rpsl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import org.apache.baremaps.calcite.DataColumn;
import org.apache.baremaps.calcite.DataRow;
import org.apache.baremaps.calcite.DataSchema;
import org.apache.baremaps.calcite.DataTable;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RpslDataTableTest {

  private DataTable dataTable;

  @BeforeEach
  public void before() throws IOException {
    var file = TestFiles.resolve("baremaps-testing/data/ripe/sample.txt");
    var inputStream = Files.newInputStream(file);
    dataTable = new RpslDataTable(inputStream);
  }

  @Test
  void schema() {
    DataSchema schema = dataTable.schema();
    assertEquals("RpslObject", schema.name());
    List<DataColumn> columns = schema.columns();
    assertTrue(columns.stream().anyMatch(c -> c.name().equals("type")));
    assertTrue(columns.stream().anyMatch(c -> c.name().equals("id")));
    assertTrue(columns.stream().anyMatch(c -> c.name().equals("inetnum")));
    assertTrue(columns.stream().anyMatch(c -> c.name().equals("inet6num")));
  }

  @Test
  void dataRows() {
    Iterator<DataRow> iterator = dataTable.iterator();

    // First object
    assertTrue(iterator.hasNext());
    DataRow row1 = iterator.next();

    System.out.println(row1);
  }

}
