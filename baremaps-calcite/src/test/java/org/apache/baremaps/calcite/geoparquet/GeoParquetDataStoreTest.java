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

package org.apache.baremaps.calcite.geoparquet;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class GeoParquetDataStoreTest {

  @Test
  void schema() {
    var uri = TestFiles.resolve("baremaps-testing/data/samples/example.parquet").toUri();
    var geoParquetDataSchema = new GeoParquetDataStore(uri);
    var table = geoParquetDataSchema.get(uri.toString());
    var rowType = table.schema();
    assertEquals(uri.toString(), rowType.name());
    assertEquals(7, rowType.columns().size());
  }

  @Test
  void read() {
    var uri = TestFiles.resolve("baremaps-testing/data/samples/example.parquet").toUri();
    var geoParquetDataSchema = new GeoParquetDataStore(uri);
    var table = geoParquetDataSchema.get(uri.toString());
    assertEquals(5, table.size());
    assertEquals(5, table.stream().count());
  }

}
