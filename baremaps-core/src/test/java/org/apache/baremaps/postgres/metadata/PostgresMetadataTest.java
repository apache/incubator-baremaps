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

package org.apache.baremaps.postgres.metadata;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.baremaps.postgres.PostgresUtils;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class PostgresMetadataTest extends PostgresContainerTest {

  @Test
  @Tag("integration")
  void resetDatabase() throws SQLException, IOException {
    try (Connection connection = DriverManager.getConnection(jdbcUrl())) {
      PostgresUtils.executeResource(connection, "queries/osm_create_extensions.sql");
      PostgresUtils.executeResource(connection, "queries/osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "queries/osm_drop_tables.sql");
      PostgresUtils.executeResource(connection, "queries/osm_create_tables.sql");
      assertTrue(tableExists("osm_headers"));
      assertTrue(tableExists("osm_nodes"));
      assertTrue(tableExists("osm_ways"));
      assertTrue(tableExists("osm_relations"));
    }
  }

  boolean tableExists(String table) throws SQLException {
    try (Connection connection = DriverManager.getConnection(jdbcUrl())) {
      DatabaseMetaData metadata = connection.getMetaData();
      ResultSet tables = metadata.getTables(null, null, table, null);
      return tables.next();
    }
  }
}
