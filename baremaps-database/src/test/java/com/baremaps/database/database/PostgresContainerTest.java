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

package com.baremaps.database.database;

import com.baremaps.database.postgres.PostgresUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresContainerTest {

  private PostgreSQLContainer container;

  @BeforeEach
  public void before() {
    var postgis =
        DockerImageName.parse("postgis/postgis:13-3.1").asCompatibleSubstituteFor("postgres");
    container = new PostgreSQLContainer(postgis);
    container.start();
  }

  @AfterEach
  public void after() {
    container.stop();
  }

  public DataSource initDataSource() throws SQLException, IOException {
    DataSource dataSource = PostgresUtils.dataSource(getJdbcUrl(), 1);
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
    }
    return dataSource;
  }

  public DataSource postgresDataSource() throws SQLException, IOException {
    var dataSource = new PGSimpleDataSource();
    dataSource.setUrl(getJdbcUrl());
    try (Connection connection = dataSource.getConnection()) {
      PostgresUtils.executeResource(connection, "osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "osm_create_tables.sql");
    }
    return dataSource;
  }

  public String getJdbcUrl() {
    return String.format(
        "%s&user=%s&password=%s&currentSchema=%s",
        container.getJdbcUrl(), container.getUsername(), container.getPassword(), "public");
  }
}
