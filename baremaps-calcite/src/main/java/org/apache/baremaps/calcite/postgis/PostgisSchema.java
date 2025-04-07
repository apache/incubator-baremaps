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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.baremaps.postgres.metadata.DatabaseMetadata;
import org.apache.baremaps.postgres.metadata.TableMetadata;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

/**
 * A Calcite schema backed by PostGIS.
 * 
 * <p>
 * This schema exposes tables in a PostgreSQL database with PostGIS extensions as Calcite tables.
 * </p>
 */
public class PostgisSchema extends AbstractSchema {

  private final DataSource dataSource;
  private final String schemaName;
  private Map<String, Table> tableMap;

  /**
   * Creates a PostgisSchema.
   *
   * @param dataSource the data source
   * @param schemaName the schema name
   */
  public PostgisSchema(DataSource dataSource, String schemaName) {
    this.dataSource = dataSource;
    this.schemaName = schemaName;
  }

  @Override
  protected Map<String, Table> getTableMap() {
    if (tableMap == null) {
      tableMap = new HashMap<>();

      try {
        // Use DatabaseMetadata to get all tables in the schema
        DatabaseMetadata metadata = new DatabaseMetadata(dataSource);
        var tables = metadata.getTableMetaData(null, schemaName, null, new String[] {"BASE TABLE"});

        // Find tables with geometry columns
        Set<String> tablesWithGeometry = findTablesWithGeometry();

        // Create a PostgisTable for each table with geometry columns
        for (TableMetadata tableMetadata : tables) {
          String tableName = tableMetadata.table().tableName();
          if (tablesWithGeometry.contains(tableName)) {
            tableMap.put(tableName, new PostgisTable(dataSource, tableName));
          }
        }
      } catch (SQLException e) {
        throw new RuntimeException("Error getting tables from schema: " + schemaName, e);
      }
    }

    return tableMap;
  }

  /**
   * Find all tables that have geometry columns.
   *
   * @return set of table names with geometry columns
   * @throws SQLException if an error occurs
   */
  private Set<String> findTablesWithGeometry() throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      // Query to get all tables with geometry columns
      String sql = "SELECT DISTINCT f_table_name FROM geometry_columns WHERE f_table_schema = ?";

      try (var stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, schemaName);

        try (ResultSet rs = stmt.executeQuery()) {
          Set<String> result = new java.util.HashSet<>();
          while (rs.next()) {
            result.add(rs.getString("f_table_name"));
          }
          return result;
        }
      }
    }
  }
}
