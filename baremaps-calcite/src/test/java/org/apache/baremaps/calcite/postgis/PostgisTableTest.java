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
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.baremaps.calcite.data.DataSchema;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Tests for the PostgisTable class, which provides access to PostgreSQL/PostGIS tables through the
 * Calcite framework for SQL querying.
 */
class PostgisTableTest extends PostgresContainerTest {

  private static final String TEST_TABLE = "postgis_test";

  @BeforeEach
  void setUp() throws SQLException {
    DataSource dataSource = dataSource();
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      // Ensure PostGIS extension is available
      statement.execute("CREATE EXTENSION IF NOT EXISTS postgis");

      // Drop the test table if it exists
      statement.execute("DROP TABLE IF EXISTS " + TEST_TABLE);

      // Create a test table with various data types including a geometry column
      statement.execute("CREATE TABLE " + TEST_TABLE + " (" +
          "id INTEGER PRIMARY KEY, " +
          "name VARCHAR(100), " +
          "description TEXT, " +
          "is_active BOOLEAN, " +
          "height DOUBLE PRECISION, " +
          "created_at TIMESTAMP, " +
          "geometry GEOMETRY(Point, 4326)" +
          ")");

      // Insert test data
      statement.execute("INSERT INTO " + TEST_TABLE +
          " VALUES (1, 'Point 1', 'First test point', true, 123.45, NOW(), " +
          "ST_SetSRID(ST_MakePoint(10.5, 20.5), 4326))");
      statement.execute("INSERT INTO " + TEST_TABLE +
          " VALUES (2, 'Point 2', 'Second test point', false, 67.89, NOW(), " +
          "ST_SetSRID(ST_MakePoint(-5.5, 30.2), 4326))");
    }
  }

  @Nested
  class SchemaTests {

    @Test
    @Tag("integration")
    void schemaContainsExpectedColumns() throws Exception {
      PostgisTable table = new PostgisTable(dataSource(), TEST_TABLE);
      DataSchema schema = table.schema();

      assertNotNull(schema, "Schema should not be null");
      assertEquals(TEST_TABLE, schema.name(), "Schema should have correct name");
      assertEquals(7, schema.columns().size(), "Schema should have 7 columns");

      // Verify column names
      assertTrue(schema.columns().stream().anyMatch(c -> c.name().equals("id")),
          "Schema should have 'id' column");
      assertTrue(schema.columns().stream().anyMatch(c -> c.name().equals("name")),
          "Schema should have 'name' column");
      assertTrue(schema.columns().stream().anyMatch(c -> c.name().equals("geometry")),
          "Schema should have 'geometry' column");
    }

    @Test
    @Tag("integration")
    void rowTypeMatchesSchema() throws Exception {
      RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
      PostgisTable table = new PostgisTable(dataSource(), TEST_TABLE, typeFactory);
      RelDataType rowType = table.getRowType(typeFactory);

      assertNotNull(rowType, "Row type should not be null");
      assertEquals(7, rowType.getFieldCount(), "Row type should have 7 fields");

      // Verify the field names in the row type
      assertTrue(rowType.getFieldNames().contains("id"), "Row type should contain 'id' field");
      assertTrue(rowType.getFieldNames().contains("geometry"),
          "Row type should contain 'geometry' field");
    }
  }

  @Nested
  class SqlQueryTests {

    private Connection connection;
    private CalciteConnection calciteConnection;

    @BeforeEach
    void setUpCalciteConnection() throws Exception {
      Class.forName("org.apache.calcite.jdbc.Driver");
      Properties info = new Properties();
      info.setProperty("lex", "JAVA");
      info.setProperty("quoting", "DOUBLE_QUOTE");

      connection = DriverManager.getConnection("jdbc:calcite:", info);
      calciteConnection = connection.unwrap(CalciteConnection.class);

      // Add our PostGIS table to the schema
      PostgisTable postgisTable = new PostgisTable(dataSource(), TEST_TABLE);
      calciteConnection.getRootSchema().add(TEST_TABLE, postgisTable);
    }

    @Test
    @Tag("integration")
    void simpleSelectReturnsExpectedResults() throws Exception {
      try (Statement statement = calciteConnection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT id, name, is_active FROM " + TEST_TABLE + " ORDER BY id")) {

        // Verify first row
        assertTrue(resultSet.next(), "Should have at least one row");
        assertEquals(1, resultSet.getInt("id"));
        assertEquals("Point 1", resultSet.getString("name"));
        assertTrue(resultSet.getBoolean("is_active"));

        // Verify second row
        assertTrue(resultSet.next(), "Should have a second row");
        assertEquals(2, resultSet.getInt("id"));
        assertEquals("Point 2", resultSet.getString("name"));
        assertFalse(resultSet.getBoolean("is_active"));

        // No more rows
        assertFalse(resultSet.next(), "Should have only two rows");
      }
    }

    @Test
    @Tag("integration")
    void selectWithGeometryColumn() throws Exception {
      try (Statement statement = calciteConnection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT id, name, geometry FROM " + TEST_TABLE + " ORDER BY id")) {

        // Verify first row with geometry
        assertTrue(resultSet.next(), "Should have at least one row");
        assertEquals(1, resultSet.getInt("id"));
        Object geometry = resultSet.getObject("geometry");
        assertNotNull(geometry, "Geometry should not be null");
        assertTrue(geometry instanceof Geometry, "Should get a JTS Geometry object");
        assertTrue(geometry instanceof Point, "Should be a Point geometry");

        // Verify second row with geometry
        assertTrue(resultSet.next(), "Should have a second row");
        assertEquals(2, resultSet.getInt("id"));
        geometry = resultSet.getObject("geometry");
        assertNotNull(geometry, "Geometry should not be null");
        assertTrue(geometry instanceof Geometry, "Should get a JTS Geometry object");
        assertTrue(geometry instanceof Point, "Should be a Point geometry");
      }
    }

    @Test
    @Tag("integration")
    void filteringByColumns() throws Exception {
      try (Statement statement = calciteConnection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT id, name FROM " + TEST_TABLE + " WHERE is_active = true")) {

        // Should only have the first row
        assertTrue(resultSet.next(), "Should have one matching row");
        assertEquals(1, resultSet.getInt("id"));
        assertEquals("Point 1", resultSet.getString("name"));

        // No more rows
        assertFalse(resultSet.next(), "Should have only one matching row");
      }
    }
  }
}
