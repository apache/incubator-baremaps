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

package com.baremaps.core.postgres;

import com.google.common.io.Resources;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/** A helper class for creating data sources and executing queries. */
public final class PostgresUtils {

  private PostgresUtils() {}

  /**
   * Creates a data source from a JDBC url with a pool size corresponding to the number of available
   * processors.
   *
   * @param url the JDBC url
   * @return the data source
   */
  public static DataSource datasource(String url) {
    return datasource(url, Runtime.getRuntime().availableProcessors());
  }

  /**
   * Creates a data source from a JDBC url with a pool size defined by the user.
   *
   * @param url the JDBC url
   * @param poolSize the pool size
   * @return the data source
   */
  public static DataSource datasource(String url, int poolSize) {
    if (poolSize < 1) {
      throw new IllegalArgumentException("PoolSize cannot be inferior to 1");
    }
    HikariConfig config = new HikariConfig();
    config.setPoolName("BaremapsDataSource");
    config.setJdbcUrl(url);
    config.setMaximumPoolSize(poolSize);
    return new HikariDataSource(config);
  }

  /**
   * Executes the queries contained in a resource file.
   *
   * @param connection the JDBC connection
   * @param resource the path of the resource file
   * @throws IOException
   * @throws SQLException
   */
  public static void executeResource(Connection connection, String resource)
      throws IOException, SQLException {
    URL resourceURL = Resources.getResource(resource);
    String queries = Resources.toString(resourceURL, StandardCharsets.UTF_8);
    try (Statement statement = connection.createStatement()) {
      statement.execute(queries);
    }
  }
}
