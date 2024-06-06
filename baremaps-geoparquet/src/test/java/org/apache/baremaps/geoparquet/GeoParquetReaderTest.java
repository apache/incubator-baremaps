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

package org.apache.baremaps.geoparquet;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GeoParquetReaderTest {

  @Test
  void read() {
    URI geoParquet = TestFiles.GEOPARQUET.toUri();
    final boolean isPrintingContent = true;
    final int expectedGroupCount = 5;

    readGroups(geoParquet, isPrintingContent, expectedGroupCount);
  }

   @Disabled("Takes too long. Around 7 minutes.")
  @Test
  void readExternal() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=admins/type=locality_area/*.parquet");
    final boolean isPrintingContent = false;
    final int expectedGroupCount = 974708;

    readGroups(geoParquet, isPrintingContent, expectedGroupCount);
  }

  private static void readGroups(URI geoParquet, boolean isPrintingContent,
      int expectedGroupCount) {
    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    final AtomicInteger groupCount = new AtomicInteger();
    geoParquetReader.read().forEach(group -> {
      groupCount.getAndIncrement();
      if (isPrintingContent) {
        System.out.println("-----");
        System.out.println(group.getSchema());
        System.out.println(group.getGeometryValue("geometry"));
      }
    });

    assertEquals(expectedGroupCount, groupCount.get());
  }

  @Test
  void validateSchemas() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=buildings/type=building/*.parquet");

    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    assertTrue(geoParquetReader.validateSchemasAreIdentical(), "Schemas are identical");
  }

  @Test
  void sizeForLocalities() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=admins/type=locality_area/*.parquet");
    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    assertEquals(974708L, geoParquetReader.size());
  }

  @Test
  void sizeForBuildings() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=buildings/type=building/*.parquet");
    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    assertEquals(2352441548L, geoParquetReader.size());
  }
}
