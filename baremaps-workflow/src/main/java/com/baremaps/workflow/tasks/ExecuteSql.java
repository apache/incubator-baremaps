/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.workflow.tasks;

import com.baremaps.postgres.PostgresUtils;
import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ExecuteSql(String database, String file, boolean parallel) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ExecuteSql.class);

  @Override
  public void run() {
    logger.info("Executing {} into {}", file, database);
    try (var dataSource = PostgresUtils.dataSource(database)) {
      var queries = Arrays.stream(Files.readString(Paths.get(file)).split(";"));
      if (parallel) {
        queries = queries.parallel();
      }
      queries.forEach(
          query -> {
            try (var connection = dataSource.getConnection()) {
              connection.createStatement().execute(query);
            } catch (SQLException e) {
              throw new WorkflowException(e);
            }
          });
      logger.info("Finished executing {} into {}", file, database);
    } catch (Exception e) {
      logger.error("Failed executing {} into {}", file, database);
      throw new WorkflowException(e);
    }
  }
}
