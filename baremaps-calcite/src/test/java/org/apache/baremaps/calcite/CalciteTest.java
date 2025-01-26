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

import java.sql.*;
import java.util.*;

import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.IndexedDataList;
import org.apache.baremaps.data.store.DataTableImpl;
import org.apache.baremaps.data.type.DataTypeImpl;
import org.apache.baremaps.store.*;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.ColumnType;
import org.apache.calcite.DataContext;
import org.apache.calcite.DataContexts;
import org.apache.calcite.interpreter.Interpreter;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.*;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class CalciteTest {

  @Test
  @Disabled
  void sql() throws SQLException {
    GeometryFactory geometryFactory = new GeometryFactory();

    // Configure Calcite connection properties
    Properties info = new Properties();
    info.setProperty("lex", "MYSQL"); // Use MySQL dialect
    info.setProperty("caseSensitive", "false"); // Disable case sensitivity
    info.setProperty("unquotedCasing", "TO_LOWER"); // Convert unquoted identifiers to lowercase
    info.setProperty("quotedCasing", "TO_LOWER");

    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      // Create and add 'city' table
      DataSchema cityRowType = new DataSchemaImpl("city", List.of(
          new DataColumnFixed("id", Cardinality.OPTIONAL, ColumnType.INTEGER),
          new DataColumnFixed("name", Cardinality.OPTIONAL, ColumnType.STRING),
          new DataColumnFixed("geometry", Cardinality.OPTIONAL, ColumnType.GEOMETRY)));

      DataTable cityDataTable = new DataTableImpl(
          cityRowType,
          new IndexedDataList<>(new AppendOnlyLog<>(new DataTypeImpl(cityRowType))));

      cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
          List.of(1, "Paris", geometryFactory.createPoint(new Coordinate(2.3522, 48.8566)))));
      cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
          List.of(2, "New York", geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))));

      BaremapsTable cityBaremapsTable = new BaremapsTable(cityDataTable);
      rootSchema.add("city", cityBaremapsTable);

      // Create and add 'population' table
      DataSchema populationRowType = new DataSchemaImpl("population", List.of(
          new DataColumnFixed("city_id", Cardinality.OPTIONAL, ColumnType.INTEGER),
          new DataColumnFixed("population", Cardinality.OPTIONAL, ColumnType.INTEGER)));

      DataTable populationDataTable = new DataTableImpl(
          populationRowType,
          new IndexedDataList<>(new AppendOnlyLog<>(new DataTypeImpl(populationRowType))));

      populationDataTable.add(new DataRowImpl(populationDataTable.schema(), List.of(1, 2_161_000)));
      populationDataTable.add(new DataRowImpl(populationDataTable.schema(), List.of(2, 8_336_000)));

      BaremapsTable populationBaremapsTable = new BaremapsTable(populationDataTable);
      rootSchema.add("population", populationBaremapsTable);

      // Create view 'city_population'
      String mvSql = "SELECT c.id, c.name, c.geometry, p.population " +
          "FROM city c " + // lowercase and unquoted
          "JOIN population p ON c.id = p.city_id";

      ViewTableMacro materializedView = MaterializedViewTable.viewMacro(
          rootSchema,
          mvSql,
          Collections.emptyList(), // Schema path
          List.of("city_population"), // Name parts
          false); // Not a materialized view


      rootSchema.add("city_population", materializedView);

      // Debug: List all tables in the root schema
      System.out.println("Available tables in the root schema:");
      for (String tableName : rootSchema.getTableNames()) {
        System.out.println(" - " + tableName);
      }

      String sql = "SELECT * FROM city";
      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(sql)) {
        while (resultSet.next()) {
          System.out.println(resultSet.getString("id") + " " + resultSet.getString("geometry"));
        }
      }

      // Query the view
      sql = "SELECT * FROM city_population";
      try (Statement statement = connection.createStatement();
          ResultSet resultSet = statement.executeQuery(sql)) {
        while (resultSet.next()) {
          System.out.println(
              resultSet.getString("id") + " " + resultSet.getString("name"));
        }
      }
    }

  }

  public class ListTable extends AbstractTable implements ScannableTable {
    private final List<Integer> data;

    public ListTable(List<Integer> data) {
      this.data = data;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
      // Define a single column named "value" of type INTEGER
      return typeFactory.builder()
          .add("V", SqlTypeName.INTEGER)
          .build();
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
      // Convert the List<Integer> to Enumerable<Object[]>
      return Linq4j.asEnumerable(data)
          .select(i -> new Object[] {i});
    }
  }

  public class ListSchema extends AbstractSchema {
    private final List<Integer> listA;
    private final List<Integer> listB;

    public ListSchema(List<Integer> listA, List<Integer> listB) {
      this.listA = listA;
      this.listB = listB;
    }

    @Override
    protected Map<String, Table> getTableMap() {
      Map<String, Table> tables = new HashMap<>();
      tables.put("LIST_A", new ListTable(listA));
      tables.put("LIST_B", new ListTable(listB)); // Initially empty
      return tables;
    }
  }

  @Test
  @Disabled
  void list() throws Exception {
    // Initialize your Java lists
    List<Integer> listA = List.of(1, 2, 3, 4, 5);
    List<Integer> listB = new ArrayList<>();

    // Set up Calcite schema
    SchemaPlus rootSchema = Frameworks.createRootSchema(true);
    rootSchema.add("MY_SCHEMA", new ListSchema(listA, listB));

    // Create and add 'city' table
    DataSchema cityRowType = new DataSchemaImpl("city", List.of(
        new DataColumnFixed("id", Cardinality.OPTIONAL, ColumnType.INTEGER),
        new DataColumnFixed("name", Cardinality.OPTIONAL, ColumnType.STRING),
        new DataColumnFixed("geometry", Cardinality.OPTIONAL, ColumnType.GEOMETRY)));

    DataTable cityDataTable = new DataTableImpl(
        cityRowType,
        new IndexedDataList<>(new AppendOnlyLog<>(new DataTypeImpl(cityRowType))));

    GeometryFactory geometryFactory = new GeometryFactory();
    cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
        List.of(1, "Paris", geometryFactory.createPoint(new Coordinate(2.3522, 48.8566)))));
    cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
        List.of(2, "New York", geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))));

    BaremapsTable cityBaremapsTable = new BaremapsTable(cityDataTable);
    rootSchema.add("CITY", cityBaremapsTable);

    // Configure the framework
    FrameworkConfig config = Frameworks.newConfigBuilder()
        .defaultSchema(rootSchema.getSubSchema("MY_SCHEMA"))
        .build();

    // Create a planner
    Planner planner = Frameworks.getPlanner(config);

    // Define the SQL query to populate list_b from list_a
    String sql = "SELECT V * 2 AS V FROM LIST_A";

    // Parse the SQL query
    org.apache.calcite.sql.SqlNode parsed = planner.parse(sql);

    // Validate the SQL query
    org.apache.calcite.sql.SqlNode validated = planner.validate(parsed);

    // Convert the SQL query to a relational expression
    RelNode rel = planner.rel(validated).rel;

    Interpreter interpreter = new Interpreter(DataContexts.EMPTY, rel);

    // Create an interpreter to execute the RelNode
    for (Object[] row : interpreter) {
      listB.add((Integer) row[0]);
    }

    // Display the results
    System.out.println("List A: " + listA);
    System.out.println("List B (after SQL): " + listB);
  }


}
