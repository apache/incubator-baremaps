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

package org.apache.baremaps.workflow;



import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.apache.baremaps.database.PostgresUtils;

/**
 * A context that is passed to the tasks of a workflow and used to share data between tasks.
 */
public class WorkflowContext {

  private Map<String, DataSource> dataSources = new ConcurrentHashMap<>() {};

  /**
   * Returns the data source associated with the specified database.
   *
   * @param database the JDBC connection string to the database
   * @return the data source
   */
  public DataSource getDataSource(String database) {
    return dataSources.computeIfAbsent(database, d -> PostgresUtils.dataSource(d));
  }

}
