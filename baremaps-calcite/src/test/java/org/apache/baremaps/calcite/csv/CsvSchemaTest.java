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

package org.apache.baremaps.calcite.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import org.apache.baremaps.testing.TestFiles;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CsvSchemaTest {

  private static final File SAMPLE_CSV_DIR = TestFiles.SAMPLE_CSV_DIR.toFile();
  private static final char SEPARATOR = ',';
  private static final boolean HAS_HEADER = true;

  @BeforeAll
  static void setup() throws IOException {
    // Ensure the test directory exists
    SAMPLE_CSV_DIR.mkdirs();

    // Create test CSV files if they don't exist
    if (!TestFiles.CITIES_CSV.toFile().exists()) {
      // Create a sample cities.csv file
      String citiesContent = "city,country,population\n"
          + "Paris,France,2148000\n"
          + "London,UK,8982000\n"
          + "Tokyo,Japan,37400000\n";
      Files.writeString(TestFiles.CITIES_CSV, citiesContent);
    }

    if (!TestFiles.COUNTRIES_CSV.toFile().exists()) {
      // Create a sample countries.csv file
      String countriesContent = "country,continent,population\n"
          + "France,Europe,67390000\n"
          + "UK,Europe,67220000\n"
          + "Japan,Asia,125700000\n";
      Files.writeString(TestFiles.COUNTRIES_CSV, countriesContent);
    }
  }

  @Test
  void testSchemaCreation() throws IOException {
    // Create a CsvSchema instance
    CsvSchema schema = new CsvSchema(SAMPLE_CSV_DIR, SEPARATOR, HAS_HEADER);

    // Get the table map
    Map<String, Table> tableMap = schema.getTableMap();

    // Verify that the schema has tables
    assertNotNull(tableMap);
    assertFalse(tableMap.isEmpty(), "Schema should have at least one table");

    // Verify that both test tables exist
    assertTrue(tableMap.containsKey("cities"), "Schema should contain the 'cities' table");
    assertTrue(tableMap.containsKey("countries"), "Schema should contain the 'countries' table");
    assertNotNull(tableMap.get("cities"), "Cities table should not be null");
    assertNotNull(tableMap.get("countries"), "Countries table should not be null");
  }

  @Test
  void testSqlQueryWithSchema() throws Exception {
    // Create a CsvSchema instance
    CsvSchema schema = new CsvSchema(SAMPLE_CSV_DIR, SEPARATOR, HAS_HEADER);

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");

    // Set up a connection and register our schema
    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Register the schema
      rootSchema.add("csv", schema);

      // Execute a simple query
      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT * FROM csv.cities WHERE country = 'France'")) {

        // Verify that we get results
        assertTrue(resultSet.next(), "Should have at least one row");
        assertEquals("Paris", resultSet.getString("city"));
        assertEquals("France", resultSet.getString("country"));
        assertEquals("2148000", resultSet.getString("population"));
      }
    }
  }

  @Test
  void testJoinQuery() throws Exception {
    // Create a CsvSchema instance
    CsvSchema schema = new CsvSchema(SAMPLE_CSV_DIR, SEPARATOR, HAS_HEADER);

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");

    // Set up a connection and register our schema
    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Register the schema
      rootSchema.add("csv", schema);

      // Execute a join query
      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(
              "SELECT c.city, c.country, co.continent "
                  + "FROM csv.cities c "
                  + "JOIN csv.countries co ON c.country = co.country "
                  + "WHERE co.continent = 'Europe'")) {

        // Verify first row
        assertTrue(resultSet.next(), "Should have at least one row");
        assertEquals("Paris", resultSet.getString("city"));
        assertEquals("France", resultSet.getString("country"));
        assertEquals("Europe", resultSet.getString("continent"));

        // Verify second row
        assertTrue(resultSet.next(), "Should have a second row");
        assertEquals("London", resultSet.getString("city"));
        assertEquals("UK", resultSet.getString("country"));
        assertEquals("Europe", resultSet.getString("continent"));

        // Should be no more rows for Europe
        assertFalse(resultSet.next(), "Should have no more rows");
      }
    }
  }
}
