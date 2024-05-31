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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GeoParquetReaderTest {

  @Test
  void read() throws IOException, URISyntaxException {
    URI geoParquet = TestFiles.GEOPARQUET.toUri();
    final boolean isPrintingContent = true;
    final int expectedGroupCount = 5;

    readGroups(geoParquet, isPrintingContent, expectedGroupCount);
  }

  @Disabled("Takes too long. Around 10 minutes.")
  @Test
  void readExternal() throws IOException, URISyntaxException {
    URI geoParquet = new URI(
        "s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=admins/type=locality_area/*.parquet");
    final boolean isPrintingContent = false;
    final int expectedGroupCount = 974708;

    readGroups(geoParquet, isPrintingContent, expectedGroupCount);
  }

  private static void readGroups(URI geoParquet, boolean isPrintingContent, int expectedGroupCount)
      throws IOException, URISyntaxException {
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
}
