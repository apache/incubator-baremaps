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

package org.apache.baremaps.tasks;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.baremaps.postgres.graph.DatabaseMetadataRetriever;
import org.apache.baremaps.postgres.graph.DependencyGraphBuilder;
import org.apache.baremaps.postgres.graph.MaterializedViewRefresher;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshMaterializedViews implements Task {

  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshMaterializedViews.class);

  private Object database;

  public RefreshMaterializedViews() {
    // Default constructor
  }

  public RefreshMaterializedViews(Object database) {
    this.database = database;
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    DataSource dataSource = context.getDataSource(database);
    try (var connection = dataSource.getConnection()) {
      LOGGER.info("Connected to PostgreSQL database.");
      var schema = "public";

      // 1. Retrieve database objects (tables, views, materialized views).
      var objects = DatabaseMetadataRetriever.getObjects(connection, schema);

      // 2. Retrieve dependencies between database objects.
      var dependencies = DatabaseMetadataRetriever.getDependencies(connection, schema, objects);

      // 3. Build a directed graph of dependencies between the database objects.
      var graph = DependencyGraphBuilder.buildGraph(connection, schema, objects, dependencies);

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
