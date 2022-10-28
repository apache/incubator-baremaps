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

package org.apache.baremaps.iploc.database;



import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** A helper class for creating executing sql scripts onto a SQLite database */
public final class SqliteUtils {

  /**
   * Executes the queries contained in a resource file.
   *
   * @param databaseUrl the JDBC url
   * @param resource the path of the resource file
   * @throws SQLException
   */
  public static void executeResource(String databaseUrl, String resource)
      throws IOException, SQLException {
    try (Connection connection = DriverManager.getConnection(databaseUrl)) {
      URL resourceURL = Resources.getResource(resource);
      String queries = Resources.toString(resourceURL, StandardCharsets.UTF_8);
      try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate(queries);
      }
    }
  }
}
