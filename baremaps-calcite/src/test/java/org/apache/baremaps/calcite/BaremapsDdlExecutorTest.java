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

package org.apache.baremaps.calcite;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import org.apache.baremaps.calcite.data.*;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataCollection;
import org.apache.baremaps.data.memory.MemoryMappedDirectory;
import org.apache.baremaps.testing.FileUtils;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class BaremapsDdlExecutorTest {

  private static final String CITY_DATA_DIR = "city_data";
  private static final String CITY_POPULATION_DIR = "city_population";
  private static final String POPULATION_DATA_DIR = "population_data";
  private DataCollection<DataRow> cityCollection;
  private DataCollection<DataRow> populationCollection;

  @BeforeEach
  void setUp() throws IOException {
    // Create and initialize city collection
    MemoryMappedDirectory cityMemory = new MemoryMappedDirectory(Paths.get(CITY_DATA_DIR));
    DataTableSchema citySchema = createCityGetSchema();
    DataRowType cityRowType = new DataRowType(citySchema);
    cityCollection = AppendOnlyLog.<DataRow>builder()
        .dataType(cityRowType)
        .memory(cityMemory)
        .build();

    // Create and initialize population collection
    MemoryMappedDirectory populationMemory =
        new MemoryMappedDirectory(Paths.get(POPULATION_DATA_DIR));
    DataTableSchema populationSchema = createPopulationGetSchema();
    DataRowType populationRowType = new DataRowType(populationSchema);
    populationCollection = AppendOnlyLog.<DataRow>builder()
        .dataType(populationRowType)
        .memory(populationMemory)
        .build();
  }

  @AfterEach
  void tearDown() throws IOException {
    // Clean up directories
    FileUtils.deleteRecursively(Paths.get(CITY_DATA_DIR).toFile());
    FileUtils.deleteRecursively(Paths.get(POPULATION_DATA_DIR).toFile());
    FileUtils.deleteRecursively(Paths.get(CITY_POPULATION_DIR).toFile());
  }

  private DataTableSchema createCityGetSchema() {
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    return new DataTableSchema("city", List.of(
        new DataColumnFixed("id", DataColumn.Cardinality.REQUIRED,
            typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.INTEGER)),
        new DataColumnFixed("name", DataColumn.Cardinality.OPTIONAL,
            typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.VARCHAR)),
        new DataColumnFixed("geometry", DataColumn.Cardinality.OPTIONAL,
            typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.GEOMETRY))));
  }

  private DataTableSchema createPopulationGetSchema() {
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
    return new DataTableSchema("population", List.of(
        new DataColumnFixed("city_id", DataColumn.Cardinality.REQUIRED,
            typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.INTEGER)),
        new DataColumnFixed("population", DataColumn.Cardinality.OPTIONAL,
            typeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName.INTEGER))));
  }

  @Test
  void testMaterializedView() throws SQLException {
    GeometryFactory geometryFactory = new GeometryFactory();
    RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL");
    info.setProperty("caseSensitive", "false");
    info.setProperty("unquotedCasing", "TO_LOWER");
    info.setProperty("quotedCasing", "TO_LOWER");
    info.setProperty("parserFactory", BaremapsDdlExecutor.class.getName() + "#PARSER_FACTORY");
    info.setProperty("materializationsEnabled", "true");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Create the city table
      DataModifiableTable cityTable = new DataModifiableTable(
          "city",
          createCityGetSchema(),
          cityCollection,
          typeFactory);

      // Add data to the city table
      Point parisPoint = geometryFactory.createPoint(new Coordinate(2.3522, 48.8566));
      Point nyPoint = geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128));

      cityCollection.add(new DataRow(createCityGetSchema(), List.of(1, "Paris", parisPoint)));
      cityCollection.add(new DataRow(createCityGetSchema(), List.of(2, "New York", nyPoint)));

      // Add city table to the schema
      rootSchema.add("city", cityTable);

      // Create the population table
      DataModifiableTable populationTable = new DataModifiableTable(
          "population",
          createPopulationGetSchema(),
          populationCollection,
          typeFactory);

      // Add data to the population table
      populationCollection.add(new DataRow(createPopulationGetSchema(), List.of(1, 2_161_000)));
      populationCollection.add(new DataRow(createPopulationGetSchema(), List.of(2, 8_336_000)));

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
    }
  }
}
