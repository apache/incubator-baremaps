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

package org.apache.baremaps.postgres.graph;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for refreshing materialized views in a robust, modular way.
 */
public class RefreshApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshApp.class.getName());

  private static final String DB_URL = "jdbc:postgresql://localhost:5432/baremaps";
  private static final String DB_USER = "baremaps";
  private static final String DB_PASSWORD = "baremaps";
  private static final String SCHEMA = "public";

  public static void main(String[] args) {
    try (var connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      LOGGER.info("Connected to PostgreSQL database.");

      // 1. Retrieve database objects (tables, views, materialized views).
      var objects = DatabaseMetadataRetriever.getObjects(connection, SCHEMA);

      // 2. Retrieve dependencies between database objects.
      var dependencies = DatabaseMetadataRetriever.getDependencies(connection, SCHEMA, objects);

      // 3. Build a directed graph of dependencies between the database objects.
      var graph = DependencyGraphBuilder.buildGraph(connection, SCHEMA, objects, dependencies);

      // 4. Perform a topological sort so that dependencies come before dependents.
      var sorted = DependencyGraphBuilder.topologicalSort(graph);

      // 5. Refresh materialized views, dropping and recreating indexes if present.
      MaterializedViewRefresher.refreshMaterializedViews(connection, sorted);

      LOGGER.info("Done refreshing materialized views.");
    } catch (SQLException ex) {
      LOGGER.error("Database error", ex);
    }
  }
}
