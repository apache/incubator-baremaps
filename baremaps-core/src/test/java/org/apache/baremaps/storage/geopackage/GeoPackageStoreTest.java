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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class GeoPackageStoreTest {

  @Test
  void schema() {
    var geoPackageStore = new GeoPackageStore(TestFiles.resolve("countries.gpkg"));
    var table = geoPackageStore.get("countries");
    var schema = table.schema();
    assertEquals(schema.name(), "countries");
    assertEquals(schema.columns().size(), 4);
  }

  @Test
  void read() {
    var geoPackageStore = new GeoPackageStore(TestFiles.resolve("countries.gpkg"));
    var table = geoPackageStore.get("countries");
    assertEquals(179, table.sizeAsLong());
    assertEquals(179, table.stream().count());
  }
}
