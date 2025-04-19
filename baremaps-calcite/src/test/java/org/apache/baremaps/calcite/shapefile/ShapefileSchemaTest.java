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

package org.apache.baremaps.calcite.shapefile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.baremaps.testing.TestFiles;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for the ShapefileSchema implementation.
 */
public class ShapefileSchemaTest {

  @TempDir
  Path tempDir;

  private File shapefileDir;
  private RelDataTypeFactory typeFactory;
  private ShapefileSchema schema;

  @BeforeEach
  public void setup() throws SQLException, IOException {
    // Create a temporary directory for test files
    Path testDir = tempDir.resolve("shapefile-test");
    Files.createDirectories(testDir);

    // Copy the sample shapefile to the test directory
    Path sourceFile = TestFiles.POINT_SHP;
    Path targetFile = testDir.resolve("point.shp");
    Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

    // Copy the associated .dbf file
    Path sourceDbf = sourceFile.resolveSibling("point.dbf");
    Path targetDbf = targetFile.resolveSibling("point.dbf");
    Files.copy(sourceDbf, targetDbf, StandardCopyOption.REPLACE_EXISTING);

    // Copy the associated .shx file
    Path sourceShx = sourceFile.resolveSibling("point.shx");
    Path targetShx = targetFile.resolveSibling("point.shx");
    Files.copy(sourceShx, targetShx, StandardCopyOption.REPLACE_EXISTING);

    shapefileDir = testDir.toFile();

    // Initialize the type factory
    Properties props = new Properties();
    props.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
    CalciteConnectionConfig config = new CalciteConnectionConfigImpl(props);

    // Create a connection to get the type factory
    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", props)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      typeFactory = calciteConnection.getTypeFactory();
    }
  }

  @AfterEach
  void cleanup() throws IOException {
    // Close the schema if it was created
    if (schema != null) {
      // Close all tables in the schema
      for (Table table : schema.getTableMap().values()) {
        if (table instanceof AutoCloseable) {
          try {
            ((AutoCloseable) table).close();
          } catch (Exception e) {
            // Ignore exceptions during cleanup
          }
        }
      }
    }
  }

  @Test
  public void testSchemaCreation() throws SQLException, IOException {
    // Create a connection to Calcite
    Connection connection = DriverManager.getConnection("jdbc:calcite:");
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();

    // Create and register the shapefile schema
    schema = new ShapefileSchema(shapefileDir, typeFactory);
    rootSchema.add("shapefile", schema);

    // Verify that the schema contains the expected table
    assertTrue(rootSchema.getSubSchemaNames().contains("shapefile"));
    assertTrue(rootSchema.getSubSchema("shapefile").getTableNames().contains("point"));

    connection.close();
  }

  @Test
  public void testSqlQuery() throws SQLException, IOException {
    // Create a connection to Calcite
    Connection connection = DriverManager.getConnection("jdbc:calcite:");
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();

    // Create and register the shapefile schema
    schema = new ShapefileSchema(shapefileDir, typeFactory);
    rootSchema.add("shapefile", schema);

    // Execute a simple SQL query - use lowercase for schema and table names
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery(
          "SELECT * FROM \"shapefile\".\"point\"");

      // Verify that we get results
      assertTrue(resultSet.next());

      // Verify that the result set has the expected columns
      // Note: The actual column names will depend on the sample shapefile
      assertNotNull(resultSet.getMetaData());
      assertTrue(resultSet.getMetaData().getColumnCount() > 0);
    }

    connection.close();
  }

  @Test
  public void testSingleFileSchema() throws SQLException, IOException {
    // Create a connection to Calcite
    Connection connection = DriverManager.getConnection("jdbc:calcite:");
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();

    // Get the sample shapefile
    File sampleFile = new File(shapefileDir, "point.shp");

    // Create and register the shapefile schema with a single file
    schema = new ShapefileSchema(sampleFile, typeFactory, false);
    rootSchema.add("single", schema);

    // Verify that the schema contains the expected table
    assertTrue(rootSchema.getSubSchemaNames().contains("single"));
    assertTrue(rootSchema.getSubSchema("single").getTableNames().contains("point"));

    // Execute a simple SQL query - use lowercase for schema and table names
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery(
          "SELECT * FROM \"single\".\"point\"");

      // Verify that we get results
      assertTrue(resultSet.next());

      // Verify that the result set has the expected columns
      assertNotNull(resultSet.getMetaData());
      assertTrue(resultSet.getMetaData().getColumnCount() > 0);
    }

    connection.close();
  }
}
