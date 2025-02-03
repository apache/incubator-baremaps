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

package org.apache.baremaps.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

class CsvDataTableTest {

  private File tempCsvFile;

  @BeforeEach
  void setUp() throws IOException {
    Path tempCsvFilePath = Files.createTempFile("test", ".csv");
    tempCsvFile = tempCsvFilePath.toFile();
    tempCsvFile.deleteOnExit();
  }

  @AfterEach
  void tearDown() {
    if (tempCsvFile.exists()) {
      tempCsvFile.delete();
    }
  }

  @Test
  void testCsvWithHeaderAndCommaSeparator() throws IOException {
    String csvContent = """
        id,name,geom
        1,PointA,"POINT(1 1)"
        2,PointB,"POINT(2 2)"
        """;
    Files.writeString(tempCsvFile.toPath(), csvContent);
    // Provide an explicit schema so that the id and geom fields are parsed correctly.
    List<DataColumn> columns = List.of(
        new DataColumnFixed("id", Cardinality.REQUIRED, ColumnType.INTEGER),
        new DataColumnFixed("name", Cardinality.OPTIONAL, ColumnType.STRING),
        new DataColumnFixed("geom", Cardinality.OPTIONAL, ColumnType.GEOMETRY));
    DataTable dataTable = new CsvDataTable("test_table", tempCsvFile, ',', columns, true);
    assertEquals(2, dataTable.size());
    int rowCount = 0;
    for (DataRow row : dataTable) {
      rowCount++;
      Integer id = (Integer) row.get("id");
      String name = (String) row.get("name");
      Geometry geometry = (Geometry) row.get("geom");
      assertNotNull(id);
      assertNotNull(name);
      assertNotNull(geometry);
      assertEquals("Point" + (rowCount == 1 ? "A" : "B"), name);
      assertEquals("POINT (" + rowCount + " " + rowCount + ")", geometry.toText());
    }
    assertEquals(2, rowCount);
  }

  @Test
  void testCsvWithoutHeaderAndSemicolonSeparator() throws IOException {
    String csvContent = """
        1;PointA;"POINT(1 1)"
        2;PointB;"POINT(2 2)"
        """;
    Files.writeString(tempCsvFile.toPath(), csvContent);
    List<DataColumn> columns = List.of(
        new DataColumnFixed("column1", Cardinality.REQUIRED, ColumnType.INTEGER),
        new DataColumnFixed("column2", Cardinality.OPTIONAL, ColumnType.STRING),
        new DataColumnFixed("column3", Cardinality.OPTIONAL, ColumnType.GEOMETRY));
    DataTable dataTable = new CsvDataTable("test_table", tempCsvFile, ';', columns, false);
    assertEquals(2, dataTable.size());
    int rowCount = 0;
    for (DataRow row : dataTable) {
      rowCount++;
      Integer id = (Integer) row.get("column1");
      String name = (String) row.get("column2");
      Geometry geometry = (Geometry) row.get("column3");

      // Verify data
      assertNotNull(id);
      assertNotNull(name);
      assertNotNull(geometry);

      assertEquals("Point" + (rowCount == 1 ? "A" : "B"), name);
      assertEquals("POINT (" + rowCount + " " + rowCount + ")", geometry.toText());
    }
    assertEquals(2, rowCount);
  }

  @Test
  void testCsvWithDifferentDataTypes() throws IOException {
    String csvContent = """
        int_col,double_col,bool_col,string_col
        1,1.1,true,Hello
        2,2.2,false,World
        """;
    Files.writeString(tempCsvFile.toPath(), csvContent);
    List<DataColumn> columns = List.of(
        new DataColumnFixed("int_col", Cardinality.REQUIRED, ColumnType.INTEGER),
        new DataColumnFixed("double_col", Cardinality.REQUIRED, ColumnType.DOUBLE),
        new DataColumnFixed("bool_col", Cardinality.REQUIRED, ColumnType.BOOLEAN),
        new DataColumnFixed("string_col", Cardinality.REQUIRED, ColumnType.STRING));
    DataTable dataTable = new CsvDataTable("test_table", tempCsvFile, ',', columns, true);
    assertEquals(2, dataTable.size());
    int rowCount = 0;
    for (DataRow row : dataTable) {
      rowCount++;
      Integer intCol = (Integer) row.get("int_col");
      Double doubleCol = (Double) row.get("double_col");
      Boolean boolCol = (Boolean) row.get("bool_col");
      String stringCol = (String) row.get("string_col");

      // Verify data
      assertEquals(rowCount, intCol);
      assertEquals(rowCount * 1.1, doubleCol);
      assertEquals(rowCount == 1, boolCol);
      assertEquals(rowCount == 1 ? "Hello" : "World", stringCol);
    }
    assertEquals(2, rowCount);
  }

  @Test
  void testCsvWithInvalidData() throws IOException {
    String csvContent = """
        id,name
        abc,TestName
        """;
    Files.writeString(tempCsvFile.toPath(), csvContent);
    List<DataColumn> columns = List.of(
        new DataColumnFixed("id", Cardinality.REQUIRED, ColumnType.INTEGER),
        new DataColumnFixed("name", Cardinality.OPTIONAL, ColumnType.STRING));
    // Pass the explicit columns so that "id" is attempted to be parsed as an integer.
    DataTable dataTable = new CsvDataTable("test_table", tempCsvFile, ',', columns, true);
    assertThrows(RuntimeException.class, () -> {
      for (DataRow row : dataTable) {
        // This line should throw an exception because "abc" is not a valid integer.
        row.values();
      }
    });
  }

  @Test
  void testAddAndClearUnsupportedOperations() throws IOException {
    // When using header=true the CSV file must contain at least a header row.
    String csvContent = "col1\n";
    Files.writeString(tempCsvFile.toPath(), csvContent);
    DataTable dataTable = new CsvDataTable("test_table", tempCsvFile, ',', true);
    assertThrows(UnsupportedOperationException.class, () -> dataTable.add(null));
    assertThrows(UnsupportedOperationException.class, dataTable::clear);
  }

  @Test
  void testSizeCalculation() throws IOException {
    String csvContent = """
        id,name
        1,Name1
        2,Name2
        3,Name3
        """;
    Files.writeString(tempCsvFile.toPath(), csvContent);
    // In this test we rely on the inferred (STRING) schema.
    DataTable dataTable = new CsvDataTable("test_table", tempCsvFile, ',', true);
    assertEquals(3, dataTable.size());
  }
}
