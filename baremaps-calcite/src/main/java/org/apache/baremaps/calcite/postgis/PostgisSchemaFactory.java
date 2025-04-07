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
import java.util.Map;
import javax.sql.DataSource;
import org.apache.baremaps.postgres.metadata.DatabaseMetadata;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * A factory for {@link PostgisSchema} schemas.
 * 
 * <p>This schema factory connects to a PostgreSQL database with PostGIS extensions
 * and exposes tables as Calcite tables.</p>
 */
public class PostgisSchemaFactory implements SchemaFactory {

  /**
   * Creates a {@link PostgisSchema}.
   *
   * @param parentSchema Parent schema
   * @param name Schema name
   * @param operand The schema specification
   * @return The PostGIS schema
   */
  @Override
  public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
    String jdbcUrl = (String) operand.get("jdbcUrl");
    String username = (String) operand.get("username");
    String password = (String) operand.get("password");
    String schema = (String) operand.getOrDefault("schema", "public");
    
    if (jdbcUrl == null) {
      throw new IllegalArgumentException("'jdbcUrl' must be specified");
    }

    DataSource dataSource = createDataSource(jdbcUrl, username, password);
    validatePostgisAvailability(dataSource);
    
    return new PostgisSchema(dataSource, schema);
  }

  /**
   * Creates a data source using HikariCP.
   *
   * @param jdbcUrl The JDBC URL
   * @param username The username
   * @param password The password
   * @return The data source
   */
  private static DataSource createDataSource(String jdbcUrl, String username, String password) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setMinimumIdle(1);
    config.setMaximumPoolSize(5);
    config.setAutoCommit(true);
    config.setConnectionTestQuery("SELECT 1");
    
    return new HikariDataSource(config);
  }
  
  /**
   * Validates that the database is PostgreSQL with PostGIS extension installed.
   *
   * @param dataSource The data source
   * @throws RuntimeException if validation fails
   */
  private static void validatePostgisAvailability(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetadata metadata = new DatabaseMetadata(dataSource);
      
      // Get database metadata
      java.sql.DatabaseMetaData dbMetadata = connection.getMetaData();
      String productName = dbMetadata.getDatabaseProductName();
      
      if (!"PostgreSQL".equalsIgnoreCase(productName)) {
        throw new IllegalArgumentException("Database is not PostgreSQL: " + productName);
      }
      
      // Check for PostGIS extension
      try (ResultSet rs = connection.createStatement().executeQuery(
          "SELECT postgis_version()")) {
        if (!rs.next()) {
          throw new IllegalArgumentException("PostGIS extension not installed or not working");
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("PostGIS extension not installed", e);
      }
      
      // Check for geometry_columns table
      try {
        var tables = metadata.getTableMetaData(null, "public", "geometry_columns", 
            new String[]{"TABLE", "VIEW"});
        if (tables.isEmpty()) {
          throw new IllegalArgumentException("geometry_columns table not found, PostGIS setup is incomplete");
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("Failed to query geometry_columns table", e);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to connect to PostgreSQL database", e);
    }
  }
} 