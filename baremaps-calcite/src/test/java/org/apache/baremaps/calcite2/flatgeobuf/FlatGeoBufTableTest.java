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

package org.apache.baremaps.calcite2.flatgeobuf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import org.apache.baremaps.testing.TestFiles;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.Test;

class FlatGeoBufTableTest {

  private static final File SAMPLE_FLATGEOBUF =
      TestFiles.POINT_FGB.toFile();

  @Test
  void testSchemaVerification() throws IOException {
    // Create a FlatGeoBufTable instance
    FlatGeoBufTable flatGeoBufTable = new FlatGeoBufTable(SAMPLE_FLATGEOBUF);

    // Get the schema
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    RelDataType rowType = flatGeoBufTable.getRowType(typeFactory);

    // Verify that the schema has the expected columns
    assertNotNull(rowType);
    assertTrue(rowType.getFieldCount() > 0);

    // Verify that there is a geometry column
    boolean hasGeometryColumn = false;
    for (int i = 0; i < rowType.getFieldCount(); i++) {
      if (rowType.getFieldList().get(i).getType().getSqlTypeName().getName().equals("GEOMETRY")) {
        hasGeometryColumn = true;
        break;
      }
    }
    assertTrue(hasGeometryColumn, "Schema should have a geometry column");
  }

  @Test
  void testSqlQueryWithRealFlatGeoBuf() throws Exception {
    // Create a FlatGeoBufTable instance
    FlatGeoBufTable flatGeoBufTable = new FlatGeoBufTable(SAMPLE_FLATGEOBUF);

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");

    // Set up a connection and register our table
    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Register the table in the root schema
      rootSchema.add("flatgeobuf", flatGeoBufTable);

      // Execute a simple query
      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT * FROM flatgeobuf LIMIT 5")) {

        // Verify that we get results
        assertTrue(resultSet.next(), "Should have at least one row");
      }
    }
  }
}
