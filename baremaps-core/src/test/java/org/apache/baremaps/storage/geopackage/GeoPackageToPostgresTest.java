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

package org.apache.baremaps.storage.geopackage;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.baremaps.storage.postgres.PostgresDataSchema;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class GeoPackageToPostgresTest extends PostgresContainerTest {

  @Test
  @Tag("integration")
  void schema() {
    // Open the GeoPackage
    var geoPackageSchema = new GeoPackageDataSchema(TestFiles.resolve("countries.gpkg"));
    var geoPackageTable = geoPackageSchema.get("countries");

    // Copy the table to Postgres
    var postgresStore = new PostgresDataSchema(dataSource());
    postgresStore.add(geoPackageTable);

    // Check the table in Postgres
    var postgresTable = postgresStore.get("countries");
    assertEquals("countries", postgresTable.rowType().name());
    assertEquals(4, postgresTable.rowType().columns().size());
    assertEquals(179l, postgresTable.sizeAsLong());
    assertEquals(179l, postgresTable.stream().count());
  }
}
