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

package org.apache.baremaps.openstreetmap;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import org.apache.baremaps.openstreetmap.pbf.PbfEntityReader;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class OpenStreetMapDataTableTest {

  @Test
  void schema() throws IOException {
    var uri = TestFiles.resolve("baremaps-testing/data/osm-sample/sample.osm.pbf");
    try (var inputStream = Files.newInputStream(uri)) {
      var table = new OpenStreetMapDataTable(new PbfEntityReader(), inputStream);
      var rowType = table.schema();
      assertEquals(rowType.getName(), "osm_data");
      assertEquals(9, rowType.getColumns().size());
    }
  }

  @Test
  void read() throws IOException {
    var uri = TestFiles.resolve("baremaps-testing/data/osm-sample/sample.osm.pbf");
    try (var inputStream = Files.newInputStream(uri)) {
      var table = new OpenStreetMapDataTable(new PbfEntityReader(), inputStream);
      assertEquals(Long.MAX_VALUE, table.size());
      assertEquals(36, table.stream().count());
    }
  }

}
