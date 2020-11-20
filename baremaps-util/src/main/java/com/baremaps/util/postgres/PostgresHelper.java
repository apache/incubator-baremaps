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

package com.baremaps.util.postgres;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public final class PostgresHelper {

  private PostgresHelper() {

  }

  private static final String DATABASE_URL = "jdbc:postgresql://%s:%s/%s?user=%s&password=%s&allowMultiQueries=%s";

  public static String url(
      String hostname,
      Integer port,
      String database,
      String username,
      String password,
      Boolean allowMultiQueries) {
    checkNotNull(hostname);
    checkNotNull(port);
    checkNotNull(database);
    checkNotNull(username);
    checkNotNull(password);
    checkNotNull(allowMultiQueries);
    return String.format(DATABASE_URL, hostname, port, database, username, password, allowMultiQueries);
  }

  public static String url(String database, String user, String password) {
    return url("localhost", 5432, database, user, password, true);
  }

  public static DataSource datasource(String url) {
    BasicDataSource datasource = new BasicDataSource();
    datasource.setUrl(url);
    return datasource;
  }

  public static void executeResource(Connection connection, String resource) throws IOException, SQLException {
    String queries = Resources.toString(Resources.getResource(resource), Charsets.UTF_8);
    try (Statement statement = connection.createStatement()) {
      statement.execute(queries);
    }
  }

}
