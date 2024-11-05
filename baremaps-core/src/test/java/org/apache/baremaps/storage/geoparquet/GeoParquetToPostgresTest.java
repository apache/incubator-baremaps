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

package org.apache.baremaps.storage.geoparquet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.baremaps.database.PostgresContainerTest;
import org.apache.baremaps.storage.postgres.PostgresDataStore;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class GeoParquetToPostgresTest extends PostgresContainerTest {

  @Test
  @Tag("integration")
  void copyGeoParquetToPostgres() {
    // Open the GeoParquet
    var uri = TestFiles.resolve("baremaps-testing/data/samples/example.parquet").toUri();
    var geoParquetSchema = new GeoParquetDataStore(uri);
    var tables = geoParquetSchema.list();
    var geoParquetTable = geoParquetSchema.get(tables.get(0));

    // Copy the table to Postgres
    var postgresStore = new PostgresDataStore(dataSource());
    postgresStore.add("geoparquet", geoParquetTable);

    // Check the table in Postgres
    var postgresTable = postgresStore.get("geoparquet");

    assertEquals("geoparquet", postgresTable.schema().name());
    assertEquals(7, postgresTable.schema().columns().size());
    assertEquals(5L, postgresTable.size());
    assertEquals(5L, postgresTable.stream().count());
  }
}
