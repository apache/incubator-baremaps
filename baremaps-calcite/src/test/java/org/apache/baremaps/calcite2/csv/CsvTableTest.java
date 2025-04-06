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

package org.apache.baremaps.calcite2.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CsvTableTest {

  @TempDir
  File tempDir;

  @Test
  void testSchemaVerification() throws Exception {
    // Create a sample CSV file
    File csvFile = new File(tempDir, "sample.csv");
    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("id,name,age,city\n");
      writer.write("1,John,30,New York\n");
      writer.write("2,Jane,25,Los Angeles\n");
      writer.write("3,Bob,35,Chicago\n");
    }

    // Create a CsvTable
    CsvTable csvTable = new CsvTable(csvFile, ',', true);

    // Verify the schema structure
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    RelDataType rowType = csvTable.getRowType(typeFactory);

    // Get field count
    int fieldCount = rowType.getFieldCount();

    // Verify the schema has the expected number of columns
    assertEquals(4, fieldCount, "Schema should have 4 columns");

    // Verify column names
    String[] expectedColumnNames = {"id", "name", "age", "city"};

    for (int i = 0; i < fieldCount; i++) {
      assertEquals(expectedColumnNames[i], rowType.getFieldList().get(i).getName(),
          "Column " + i + " should have name " + expectedColumnNames[i]);
    }
  }

  @Test
  void testSqlQueryWithRealCsvFile() throws Exception {
    // Create a sample CSV file
    File csvFile = new File(tempDir, "sample.csv");
    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("id,name,age,city\n");
      writer.write("1,John,30,New York\n");
      writer.write("2,Jane,25,Los Angeles\n");
      writer.write("3,Bob,35,Chicago\n");
    }

    // Create the table
    CsvTable csvTable = new CsvTable(csvFile, ',', true);

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");

    // Set up a connection and register our table
    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Add the table to the schema
      rootSchema.add("csv", csvTable);

      // Test a simple query to select all rows
      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery("SELECT * FROM csv")) {
        int rowCount = 0;

        while (resultSet.next()) {
          rowCount++;
          String id = resultSet.getString("id");
          String name = resultSet.getString("name");
          String age = resultSet.getString("age");
          String city = resultSet.getString("city");

          // Verify basic properties
          assertNotNull(id, "Row should have an ID");
          assertNotNull(name, "Row should have a name");
          assertNotNull(age, "Row should have an age");
          assertNotNull(city, "Row should have a city");
        }

        // Verify that we got the expected number of rows
        assertEquals(3, rowCount, "Should have retrieved 3 rows");
      }

      // Test a query with a filter
      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery("SELECT * FROM csv WHERE age = '30'")) {
        int rowCount = 0;

        while (resultSet.next()) {
          rowCount++;
          String id = resultSet.getString("id");
          String name = resultSet.getString("name");
          String age = resultSet.getString("age");
          String city = resultSet.getString("city");

          // Verify that this is the row we expect
          assertEquals("1", id, "ID should be 1");
          assertEquals("John", name, "Name should be John");
          assertEquals("30", age, "Age should be 30");
          assertEquals("New York", city, "City should be New York");
        }

        // Verify that we got exactly one row
        assertEquals(1, rowCount, "Should have retrieved exactly one row");
      }
    }
  }

  @Test
  void testCsvWithoutHeader() throws Exception {
    // Create a sample CSV file without a header
    File csvFile = new File(tempDir, "no_header.csv");
    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("1,John,30,New York\n");
      writer.write("2,Jane,25,Los Angeles\n");
      writer.write("3,Bob,35,Chicago\n");
    }

    // Create a CsvTable with hasHeader=false
    CsvTable csvTable = new CsvTable(csvFile, ',', false);

    // Verify the schema structure
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    RelDataType rowType = csvTable.getRowType(typeFactory);

    // Get field count
    int fieldCount = rowType.getFieldCount();

    // Verify the schema has the expected number of columns
    assertEquals(4, fieldCount, "Schema should have 4 columns");

    // Verify column names (should be column1, column2, etc.)
    for (int i = 0; i < fieldCount; i++) {
      assertEquals("column" + (i + 1), rowType.getFieldList().get(i).getName(),
          "Column " + i + " should have name column" + (i + 1));
    }
  }

  @Test
  void testCsvWithCustomSeparator() throws Exception {
    // Create a sample CSV file with a custom separator
    File csvFile = new File(tempDir, "custom_separator.csv");
    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("id|name|age|city\n");
      writer.write("1|John|30|New York\n");
      writer.write("2|Jane|25|Los Angeles\n");
      writer.write("3|Bob|35|Chicago\n");
    }

    // Create a CsvTable with a custom separator
    CsvTable csvTable = new CsvTable(csvFile, '|', true);

    // Verify the schema structure
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    RelDataType rowType = csvTable.getRowType(typeFactory);

    // Get field count
    int fieldCount = rowType.getFieldCount();

    // Verify the schema has the expected number of columns
    assertEquals(4, fieldCount, "Schema should have 4 columns");

    // Verify column names
    String[] expectedColumnNames = {"id", "name", "age", "city"};

    for (int i = 0; i < fieldCount; i++) {
      assertEquals(expectedColumnNames[i], rowType.getFieldList().get(i).getName(),
          "Column " + i + " should have name " + expectedColumnNames[i]);
    }
  }
}
