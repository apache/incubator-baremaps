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

package org.apache.baremaps.data.calcite;

import com.google.common.collect.ImmutableList;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.IndexedDataList;
import org.apache.baremaps.data.type.RowDataType;
import org.apache.baremaps.store.DataColumn.Cardinality;
import org.apache.baremaps.store.DataColumn.Type;
import org.apache.baremaps.store.*;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.model.ModelHandler;
import org.apache.calcite.runtime.SpatialTypeFunctions;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ViewTable;
import org.apache.calcite.schema.impl.ViewTableMacro;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.fun.SqlSpatialTypeFunctions;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class CalciteTest {

    @Test
    void sql() throws SQLException {
        GeometryFactory geometryFactory = new GeometryFactory();

        // Configure Calcite connection properties
        Properties info = new Properties();
        info.setProperty("lex", "MYSQL");                  // Use MySQL dialect
        info.setProperty("caseSensitive", "false");        // Disable case sensitivity
        info.setProperty("unquotedCasing", "TO_LOWER");    // Convert unquoted identifiers to lowercase
        info.setProperty("quotedCasing", "TO_LOWER");

        try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();

            // Create and add 'city' table
            DataSchema cityRowType = new DataSchemaImpl("city", List.of(
                    new DataColumnFixed("id", Cardinality.OPTIONAL, Type.INTEGER),
                    new DataColumnFixed("name", Cardinality.OPTIONAL, Type.STRING),
                    new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY)));

            DataTable cityDataTable = new BaremapsDataTable(
                    cityRowType,
                    new IndexedDataList<>(new AppendOnlyLog<>(new RowDataType(cityRowType))));

            cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
                    List.of(1, "Paris", geometryFactory.createPoint(new Coordinate(2.3522, 48.8566)))));
            cityDataTable.add(new DataRowImpl(cityDataTable.schema(),
                    List.of(2, "New York", geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))));

            SqlDataTable citySqlDataTable = new SqlDataTable(cityDataTable);
            rootSchema.add("city", citySqlDataTable);

            // Create and add 'population' table
            DataSchema populationRowType = new DataSchemaImpl("population", List.of(
                    new DataColumnFixed("city_id", Cardinality.OPTIONAL, Type.INTEGER),
                    new DataColumnFixed("population", Cardinality.OPTIONAL, Type.INTEGER)));

            DataTable populationDataTable = new BaremapsDataTable(
                    populationRowType,
                    new IndexedDataList<>(new AppendOnlyLog<>(new RowDataType(populationRowType))));

            populationDataTable.add(new DataRowImpl(populationDataTable.schema(), List.of(1, 2_161_000)));
            populationDataTable.add(new DataRowImpl(populationDataTable.schema(), List.of(2, 8_336_000)));

            SqlDataTable populationSqlDataTable = new SqlDataTable(populationDataTable);
            rootSchema.add("population", populationSqlDataTable);

            // Create view 'city_population'
            String mvSql = "SELECT c.id, c.name, c.geometry, p.population " +
                    "FROM city c " +  // lowercase and unquoted
                    "JOIN population p ON c.id = p.city_id";

            ViewTableMacro materializedView = ViewTable.viewMacro(
                    rootSchema,
                    mvSql,
                    Collections.emptyList(), // Schema path
                    List.of("city_population"), // Name parts
                    false);                  // Not a materialized view

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
                    System.out.println(resultSet.getString("id") + " " + resultSet.getString("name"));
                }
            }

            // Query the view
             sql = "SELECT * FROM city_population";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    System.out.println(
                            resultSet.getString("id") + " " + resultSet.getString("name")
                    );
                }
            }
        }

    }

    @Test
    void ddl() throws SqlParseException {

        // Example SQL script with multiple DDL statements
        String sqlScript = """
                CREATE MATERIALIZED VIEW IF NOT EXISTS my_view AS
                SELECT * FROM my_table;
                """;

        // Build a parser config that supports DDL
        SqlParser.Config config = SqlParser.configBuilder()
                .setParserFactory(SqlDdlParserImpl.FACTORY)
                .setConformance(SqlConformanceEnum.BABEL)
                .setLex(Lex.MYSQL)
                .build();

        // Create the parser from the config
        SqlParser parser = SqlParser.create(sqlScript, config);

        // Parse the script as a list of statements
        SqlNodeList sqlNodeList = parser.parseStmtList();

        // Iterate and print each parsed statement
        for (SqlNode sqlNode : sqlNodeList) {
            System.out.println("Parsed statement: " + sqlNode.toString());
        }
    }

}
