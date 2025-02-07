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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.calcite.baremaps.BaremapsDataTable;
import org.apache.baremaps.calcite.baremaps.ServerDdlExecutor;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.IndexedDataList;
import org.apache.calcite.DataContext;
import org.apache.calcite.DataContexts;
import org.apache.calcite.config.Lex;
import org.apache.calcite.interpreter.Interpreter;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.SqlCreateMaterializedView;
import org.apache.calcite.sql.ddl.SqlCreateTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class CalciteDdlTest {


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
  void test() throws Exception {
    // Initialize your Java lists
    List<Integer> listA = List.of(1, 2, 3, 4, 5);
    List<Integer> listB = new ArrayList<>();

    // Set up Calcite schema
    CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
    rootSchema.add("MY_SCHEMA", new ListSchema(listA, listB));

    // Create and add 'city' table
    DataSchema cityRowType = new DataSchema("city", List.of(
        new DataColumnFixed("id", Cardinality.OPTIONAL, Type.INTEGER),
        new DataColumnFixed("name", Cardinality.OPTIONAL, Type.STRING),
        new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY)));

    DataTable cityDataTable = new BaremapsDataTable(
        cityRowType,
        new IndexedDataList<>(new AppendOnlyLog<>(new DataRowType(cityRowType))));

    GeometryFactory geometryFactory = new GeometryFactory();
    cityDataTable.add(new DataRow(cityDataTable.schema(),
        List.of(1, "Paris", geometryFactory.createPoint(new Coordinate(2.3522, 48.8566)))));
    cityDataTable.add(new DataRow(cityDataTable.schema(),
        List.of(2, "New York", geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))));

    DataTableAdapter cityDataTableAdapter = new DataTableAdapter(cityDataTable);
    rootSchema.add("CITY", cityDataTableAdapter);

    // Create and add 'population' table
    DataSchema populationRowType = new DataSchema("population", List.of(
            new DataColumnFixed("city_id", Cardinality.OPTIONAL, Type.INTEGER),
            new DataColumnFixed("population", Cardinality.OPTIONAL, Type.INTEGER)));

    DataTable populationDataTable = new BaremapsDataTable(
            populationRowType,
            new IndexedDataList<>(new AppendOnlyLog<>(new DataRowType(populationRowType))));

    populationDataTable.add(new DataRow(populationDataTable.schema(), List.of(1, 2_161_000)));
    populationDataTable.add(new DataRow(populationDataTable.schema(), List.of(2, 8_336_000)));

    DataTableAdapter populationDataTableAdapter = new DataTableAdapter(populationDataTable);
    rootSchema.add("population", populationDataTableAdapter);

    // Configure the parser
    SqlParser.Config parserConfig = SqlParser.configBuilder()
        .setLex(Lex.MYSQL)
        .setParserFactory(ServerDdlExecutor.PARSER_FACTORY)
        .build();

    // Configure the framework
    FrameworkConfig config = Frameworks.newConfigBuilder()
        .defaultSchema(rootSchema.plus())
        .parserConfig(parserConfig)
        .build();

    // Create a planner
    Planner planner = Frameworks.getPlanner(config);

    // Define the SQL query to populate list_b from list_a
    String sql = "CREATE MATERIALIZED VIEW city_population AS "
        + "SELECT c.id, c.name, c.geometry, p.population "
        + "FROM CITY c "
        + "JOIN population p ON c.id = p.city_id";

    // Parse the SQL query
    SqlNode parsed = planner.parse(sql);

    // Create a context
    //BaremapsContext context = new BaremapsContext(new JavaTypeFactoryImpl(), rootSchema, Frameworks.);

    // Create an executor
    //ServerDdlExecutor.INSTANCE.execute((SqlCreateMaterializedView) parsed, context);

//    // Extract the select statement from the parsed SQL query
//    SqlNode select;
//    if (parsed instanceof SqlCreateMaterializedView createMaterializedView) {
//      List<SqlNode> operands = createMaterializedView.getOperandList();
//      select = operands.get(operands.size() - 1);
//      System.out.println(select);
//    } else {
//      throw new IllegalArgumentException(
//          "Expected a CREATE MATERIALIZED VIEW statement, but got: " + parsed.getKind());
//    }
//
//    // Validate the SQL query
//    SqlNode validated = planner.validate(select);
//
//    // Convert the SQL query to a relational expression
//    RelNode rel = planner.rel(validated).rel;
//
//    try (Interpreter interpreter = new Interpreter(DataContexts.EMPTY, rel)) {
//      // Create an interpreter to execute the RelNode
//      for (Object[] row : interpreter) {
//        listB.add((Integer) row[0]);
//      }
//    }
//
//    // Display the results
//    System.out.println("List A: " + listA);
//    System.out.println("List B (after SQL): " + listB);
  }
}
