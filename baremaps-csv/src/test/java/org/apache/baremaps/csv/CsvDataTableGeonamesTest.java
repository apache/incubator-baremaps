/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.csv;

import static org.apache.baremaps.testing.TestFiles.GEONAMES_CSV;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.apache.baremaps.store.*;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Point;

class CsvDataTableGeonamesTest {

  @Test
  void testGeonamesCsvDataTable() throws IOException {
    DataTable dataTable = new CsvDataTable(GEONAMES_CSV.toFile(), false);

    assertEquals(5, dataTable.size(), "DataTable should have 5 rows.");

    int rowCount = 0;
    for (DataRow row : dataTable) {
      rowCount++;

      // Extract values
      Integer id = (Integer) row.get("id");
      String name = (String) row.get("name");
      Double latitude = (Double) row.get("latitude");
      Double longitude = (Double) row.get("longitude");

      // Perform assertions for each row
      assertNotNull(id, "ID should not be null.");
      assertNotNull(name, "Name should not be null.");
      assertNotNull(latitude, "Latitude should not be null.");
      assertNotNull(longitude, "Longitude should not be null.");

      switch (id) {
        case 1:
          assertEquals("HEIG", name);
          assertEquals(1.111, latitude);
          assertEquals(1.111, longitude);
          break;
        case 2:
          assertEquals("Yverdon-les-bains", name);
          assertEquals(2.222, latitude);
          assertEquals(2.222, longitude);
          break;
        case 3:
          assertEquals("Route de Cheseaux 1", name);
          assertEquals(3.333, latitude);
          assertEquals(3.333, longitude);
          break;
        case 4:
          assertEquals("Switzerland", name);
          assertEquals(4.444, latitude);
          assertEquals(4.444, longitude);
          break;
        case 5:
          assertEquals("Switzerland", name);
          assertEquals(47.00016, latitude);
          assertEquals(8.01427, longitude);
          break;
        default:
          fail("Unexpected ID: " + id);
      }

      Point point = createPoint(longitude, latitude);
      assertNotNull(point, "Point geometry should not be null.");
    }
    assertEquals(5, rowCount, "Row count should be 5.");
  }

  private Point createPoint(Double longitude, Double latitude) {
    if (longitude != null && latitude != null) {
      return new org.locationtech.jts.geom.GeometryFactory()
          .createPoint(new org.locationtech.jts.geom.Coordinate(longitude, latitude));
    } else {
      return null;
    }
  }
}
