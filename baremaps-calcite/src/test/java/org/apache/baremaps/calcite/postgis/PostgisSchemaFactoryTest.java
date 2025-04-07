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

package org.apache.baremaps.calcite.postgis;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

/**
 * Tests for the PostgisSchemaFactory class, which creates PostGIS schemas for Calcite.
 */
class PostgisSchemaFactoryTest extends PostgresContainerTest {

  private static final String TEST_TABLE = "postgis_schema_test";

  @BeforeEach
  void setUp() throws SQLException {
    try (Connection connection = dataSource().getConnection();
        Statement statement = connection.createStatement()) {
      // Ensure PostGIS extension is available
      statement.execute("CREATE EXTENSION IF NOT EXISTS postgis");

      // Drop the test table if it exists
      statement.execute("DROP TABLE IF EXISTS " + TEST_TABLE);

      // Create a test table with a geometry column
      statement.execute("CREATE TABLE " + TEST_TABLE + " (" +
          "id INTEGER PRIMARY KEY, " +
          "name VARCHAR(100), " +
          "geometry GEOMETRY(Point, 4326)" +
          ")");

      // Insert test data
      statement.execute("INSERT INTO " + TEST_TABLE +
          " VALUES (1, 'Point 1', ST_SetSRID(ST_MakePoint(10.5, 20.5), 4326))");
      statement.execute("INSERT INTO " + TEST_TABLE +
          " VALUES (2, 'Point 2', ST_SetSRID(ST_MakePoint(-5.5, 30.2), 4326))");
    }
  }

  @Test
  @Tag("integration")
  void schemaFactoryShouldCreatePostgisSchema() throws Exception {
    // Create schema factory
    PostgisSchemaFactory schemaFactory = new PostgisSchemaFactory();

    // Create operand map with connection details
    Map<String, Object> operand = new HashMap<>();
    String url = jdbcUrl();
    String username = "test"; // Default username for testcontainers PostgreSQL
    String password = "test"; // Default password for testcontainers PostgreSQL
    operand.put("jdbcUrl", url);
    operand.put("username", username);
    operand.put("password", password);
    operand.put("schema", "public");

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    info.setProperty("quoting", "DOUBLE_QUOTE");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Create the schema and add it to the root schema
      Schema postgisSchema = schemaFactory.create(rootSchema, "POSTGIS", operand);
      rootSchema.add("POSTGIS", postgisSchema);

      // Execute a query that uses the PostGIS schema
      try (Statement statement = calciteConnection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT id, name, \"geometry\" FROM \"POSTGIS\"." + TEST_TABLE + " ORDER BY id")) {

        // Verify first row
        assertTrue(resultSet.next(), "Should have at least one row");
        assertEquals(1, resultSet.getInt("id"));
        assertEquals("Point 1", resultSet.getString("name"));
        Object geometry = resultSet.getObject("geometry");
        assertNotNull(geometry, "Geometry should not be null");
        assertTrue(geometry instanceof Geometry, "Should get a Geometry object");

        // Verify second row
        assertTrue(resultSet.next(), "Should have a second row");
        assertEquals(2, resultSet.getInt("id"));
        assertEquals("Point 2", resultSet.getString("name"));
        geometry = resultSet.getObject("geometry");
        assertNotNull(geometry, "Geometry should not be null");
        assertTrue(geometry instanceof Geometry, "Should get a Geometry object");

        // No more rows
        assertFalse(resultSet.next(), "Should have only two rows");
      }
    }
  }

  @Test
  @Tag("integration")
  void schemaFactoryShouldHandleSpatialFilter() throws Exception {
    // Create schema factory and add schema
    PostgisSchemaFactory schemaFactory = new PostgisSchemaFactory();
    Map<String, Object> operand = new HashMap<>();
    operand.put("jdbcUrl", jdbcUrl());
    operand.put("username", "test");
    operand.put("password", "test");

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    info.setProperty("quoting", "DOUBLE_QUOTE");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Create the schema and add it to the root schema
      Schema postgisSchema = schemaFactory.create(rootSchema, "POSTGIS", operand);
      rootSchema.add("POSTGIS", postgisSchema);

      // First add a special function to handle ST_Contains in Calcite
      try (Statement statement = calciteConnection.createStatement()) {
        statement.execute(
            "CREATE FUNCTION \"ST_Contains\"(geom1 GEOMETRY, geom2 GEOMETRY) " +
                "RETURNS BOOLEAN " +
                "LANGUAGE JAVA " +
                "PARAMETER STYLE SYSTEM " +
                "NO SQL " +
                "EXTERNAL NAME 'org.apache.baremaps.calcite2.postgis.PostgisFunctions.stContains'");
      }

      // Execute a query with a simple filter
      try (Statement statement = calciteConnection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT id, name FROM \"POSTGIS\"." + TEST_TABLE + " WHERE id = 1")) {

        // Should only return the first row
        assertTrue(resultSet.next(), "Should have one row");
        assertEquals(1, resultSet.getInt("id"));
        assertEquals("Point 1", resultSet.getString("name"));

        // No more rows
        assertFalse(resultSet.next(), "Should have only one row");
      }
    }
  }
}
