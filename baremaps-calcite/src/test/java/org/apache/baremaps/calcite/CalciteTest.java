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

import org.apache.baremaps.calcite.DataColumn.Cardinality;
import org.apache.baremaps.calcite.DataColumn.Type;
import org.apache.baremaps.calcite.baremaps.BaremapsDataTable;
import org.apache.baremaps.calcite.baremaps.ServerDdlExecutor;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.IndexedDataList;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.*;
import java.util.List;
import java.util.Properties;

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
        info.setProperty("parserFactory", ServerDdlExecutor.class.getName() + "#PARSER_FACTORY");
        info.setProperty("materializationsEnabled", "true");

        try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();

            // Create and add 'city' table
            DataSchema cityRowType = new DataSchema("city", List.of(
                    new DataColumnFixed("id", Cardinality.OPTIONAL, Type.INTEGER),
                    new DataColumnFixed("name", Cardinality.OPTIONAL, Type.STRING),
                    new DataColumnFixed("geometry", Cardinality.OPTIONAL, Type.GEOMETRY)));

            DataTable cityDataTable = new BaremapsDataTable(
                    cityRowType,
                    new IndexedDataList<>(new AppendOnlyLog<>(new DataRowType(cityRowType))));

            cityDataTable.add(new DataRow(cityDataTable.schema(),
                    List.of(1, "Paris", geometryFactory.createPoint(new Coordinate(2.3522, 48.8566)))));
            cityDataTable.add(new DataRow(cityDataTable.schema(),
                    List.of(2, "New York", geometryFactory.createPoint(new Coordinate(-74.0060, 40.7128)))));

            DataTableAdapter cityDataTableAdapter = new DataTableAdapter(cityDataTable);
            rootSchema.add("city", cityDataTableAdapter);

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

            String mv = "CREATE MATERIALIZED VIEW city_population AS "
                    + "SELECT c.id, c.name, c.geometry, p.population "
                    + "FROM city c "
                    + "JOIN population p ON c.id = p.city_id";

            // Execute the SQL query
            try (Statement statement = connection.createStatement()) {
                statement.execute(mv);
            }

            // Debug: List all tables in the root schema
            System.out.println("Available tables in the root schema:");
            for (String tableName : rootSchema.getTableNames()) {
                System.out.println(" - " + tableName);
            }

            String sql = "SELECT * FROM city_population";

            // Execute the SQL query
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    System.out.println(resultSet.getInt(1) + " " + resultSet.getString(2) + " " + resultSet.getString(3) + " " + resultSet.getInt(4));
                }
            }


        }

    }

}
