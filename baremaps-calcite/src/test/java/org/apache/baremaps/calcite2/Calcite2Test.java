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

package org.apache.baremaps.calcite2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import org.apache.baremaps.calcite2.data.*;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataCollection;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class Calcite2Test {

  @Test
  void testMaterializedView() throws SQLException, IOException {
    GeometryFactory geometryFactory = new GeometryFactory();
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL"); // Use MySQL dialect
    info.setProperty("caseSensitive", "false"); // Disable case sensitivity
    info.setProperty("unquotedCasing", "TO_LOWER"); // Convert unquoted identifiers to lowercase
    info.setProperty("quotedCasing", "TO_LOWER");
    info.setProperty("parserFactory", BaremapsDdlExecutor.class.getName() + "#PARSER_FACTORY");
    info.setProperty("materializationsEnabled", "true");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Create and add 'city' table
      DataSchema citySchema = new DataSchema("city", List.of(
          new DataColumnFixed("id", DataColumn.Cardinality.REQUIRED,
              typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.INTEGER)),
          new DataColumnFixed("name", DataColumn.Cardinality.OPTIONAL,
              typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.VARCHAR)),
          new DataColumnFixed("geometry", DataColumn.Cardinality.OPTIONAL,
              typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.GEOMETRY))));

      DataRowType cityRowType = new DataRowType(citySchema);

      // Create in-memory collection for city data
      DataCollection<DataRow> cityCollection = AppendOnlyLog.<DataRow>builder()
          .dataType(cityRowType)
          .memory(new MemoryMappedDirectory(Paths.get("city_data")))
          .build();

      // Create the city table
      DataModifiableTable cityTable = new DataModifiableTable(
          "city",
          citySchema,
          cityCollection,
          typeFactory);

      // Add data to the city table
      Point parisPoint = geometryFactory.createPoint(new Coordinate(2.3522, 48.8566));
      Point nyPoint = geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128));

      cityCollection.add(new DataRow(citySchema, List.of(1, "Paris", parisPoint)));
      cityCollection.add(new DataRow(citySchema, List.of(2, "New York", nyPoint)));

      // Add city table to the schema
      rootSchema.add("city", cityTable);

      // Create and add 'population' table
      DataSchema populationSchema = new DataSchema("population", List.of(
          new DataColumnFixed("city_id", DataColumn.Cardinality.REQUIRED,
              typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.INTEGER)),
          new DataColumnFixed("population", DataColumn.Cardinality.OPTIONAL,
              typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.INTEGER))));

      DataRowType populationRowType = new DataRowType(populationSchema);

      // Create in-memory collection for population data
      DataCollection<DataRow> populationCollection = AppendOnlyLog.<DataRow>builder()
          .dataType(populationRowType)
          .memory(new MemoryMappedDirectory(Paths.get("population_data")))
          .build();

      // Create the population table
      DataModifiableTable populationTable = new DataModifiableTable(
          "population",
          populationSchema,
          populationCollection,
          typeFactory);

      // Add data to the population table
      populationCollection.add(new DataRow(populationSchema, List.of(1, 2_161_000)));
      populationCollection.add(new DataRow(populationSchema, List.of(2, 8_336_000)));

      // Add population table to the schema
      rootSchema.add("population", populationTable);

      // Create a materialized view
      String mv = "CREATE MATERIALIZED VIEW city_population AS "
          + "SELECT c.id, c.name, c.geometry, p.population "
          + "FROM city c "
          + "JOIN population p ON c.id = p.city_id";

      // Execute the DDL statement to create the materialized view
      try (Statement statement = connection.createStatement()) {
        statement.execute(mv);
      }

      // Query the materialized view
      String sql = "SELECT id, name, population FROM city_population ORDER BY id";

      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(sql)) {

        // Verify the first row (Paris)
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt("id"));
        assertEquals("Paris", resultSet.getString("name"));
        assertEquals(2_161_000, resultSet.getInt("population"));

        // Verify the second row (New York)
        assertTrue(resultSet.next());
        assertEquals(2, resultSet.getInt("id"));
        assertEquals("New York", resultSet.getString("name"));
        assertEquals(8_336_000, resultSet.getInt("population"));

        // No more rows
        assertFalse(resultSet.next());
      }
    } finally {
      try {
        java.nio.file.Files.deleteIfExists(Paths.get("city_data"));
        java.nio.file.Files.deleteIfExists(Paths.get("population_data"));
      } catch (IOException e) {
        // Ignore cleanup errors
      }
    }
  }
}
