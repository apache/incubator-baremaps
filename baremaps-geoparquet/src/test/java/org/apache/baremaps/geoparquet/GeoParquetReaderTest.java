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
import java.util.stream.Stream;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class GeoParquetReaderTest {

  private static void readGroups(
      URI geoParquet,
      Envelope envelope,
      boolean parallel,
      int expectedGroupCount) {
    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet, envelope);
    final AtomicInteger groupCount = new AtomicInteger();
    Stream<GeoParquetGroup> geoParquetGroupStream;
    if (parallel) {
      geoParquetGroupStream = geoParquetReader.readParallel();
    } else {
      geoParquetGroupStream = geoParquetReader.read();
    }
    geoParquetGroupStream.forEach(group -> groupCount.getAndIncrement());
    assertEquals(expectedGroupCount, groupCount.get());
  }

  @Test
  void read() {
    URI geoParquet = TestFiles.GEOPARQUET.toUri();
    final boolean isParallel = false;
    final int expectedGroupCount = 5;
    readGroups(geoParquet, null, isParallel, expectedGroupCount);
  }

  @Test
  void readFiltered() {
    URI geoParquet = TestFiles.GEOPARQUET.toUri();
    final boolean isParallel = false;
    final int expectedGroupCount = 1;
    readGroups(geoParquet, new Envelope(-172, -65, 18, 72), isParallel, expectedGroupCount);
  }

  @Disabled("Requires access to the Internet")
  @Test
  void readExternal() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=admins/type=locality_area/*.parquet");
    final boolean isParallel = true;
    final int expectedGroupCount = 974708;

    readGroups(geoParquet, null, isParallel, expectedGroupCount);
  }

  @Disabled("Requires access to the Internet")
  @Test
  void validateSchemas() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=buildings/type=building/*.parquet");

    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    assertTrue(geoParquetReader.validateSchemasAreIdentical(), "Schemas are identical");
  }

  @Disabled("Requires access to the Internet")
  @Test
  void sizeForLocalities() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=admins/type=locality_area/*.parquet");
    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    assertEquals(974708L, geoParquetReader.size());
  }

  @Disabled("Requires access to the Internet")
  @Test
  void sizeForBuildings() throws URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=buildings/type=building/*.parquet");
    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    assertEquals(2352441548L, geoParquetReader.size());
  }
}
