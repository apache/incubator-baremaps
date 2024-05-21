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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.apache.baremaps.geoparquet.data.FeatureGroup;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class GeoParquetReaderTest {

  @Test
  void read() throws IOException {
    URI geoParquet = TestFiles.GEOPARQUET.toUri();
    System.out.println(geoParquet);
    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    List<FeatureGroup> geoParquetList = geoParquetReader.read().toList();
    for (FeatureGroup featureGroup : geoParquetList) {
      System.out.println(featureGroup);
    }
  }

}
