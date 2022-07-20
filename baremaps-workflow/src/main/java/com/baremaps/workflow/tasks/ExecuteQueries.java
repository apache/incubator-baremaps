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

import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;

public record ExecuteQueries(String database, String file) implements Task {

  @Override
  public void run() {
    var config = new HikariConfig();
    config.setPoolName("BaremapsDataSource");
    config.setJdbcUrl(database);
    config.setMaximumPoolSize(Runtime.getRuntime().availableProcessors());
    try (var dataSource = new HikariDataSource(config)) {
      Stream<String> queries = Arrays.stream(Files.readString(Paths.get(file)).split(";"));
      queries.forEach(
          query -> {
            try (var connection = dataSource.getConnection()) {
              connection.createStatement().execute(query);
            } catch (SQLException e) {
              throw new WorkflowException(e);
            }
          });
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
