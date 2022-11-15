/*
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

package org.apache.baremaps.workflow.tasks;

import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ExecuteSql(String database, String file, boolean parallel) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ExecuteSql.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Executing {}", file);
    var queries = Arrays.stream(Files.readString(Paths.get(file)).split(";"));
    if (parallel) {
      queries = queries.parallel();
    }
    queries.forEach(
      query -> {
        var dataSource = context.getDataSource(database);
        try (var connection = dataSource.getConnection()) {
          connection.createStatement().execute(query);
        } catch (SQLException e) {
          logger.error("Failed executing {}", query);
          throw new WorkflowException(e);
        }
      });
    logger.info("Finished executing {}", file);
  }
  
}
