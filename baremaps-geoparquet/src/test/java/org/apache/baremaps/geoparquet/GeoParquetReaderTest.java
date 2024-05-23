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
import org.apache.baremaps.testing.TestFiles;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.Type;
import org.junit.jupiter.api.Test;

class GeoParquetReaderTest {

  @Test
  void read() throws IOException {
    // URI geoParquet = new
    // URI("s3a://overturemaps-us-west-2/release/2024-03-12-alpha.0/theme=admins/type=locality_area/*.parquet");
    URI geoParquet = TestFiles.GEOPARQUET.toUri();

    GeoParquetReader geoParquetReader = new GeoParquetReader(geoParquet);
    geoParquetReader.read().forEach(group -> {
      GroupType schema = group.getSchema();
      for (int i = 0; i < schema.getFieldCount(); i++) {
        Type fieldType = schema.getType(i);
        if (fieldType.isPrimitive()) {
          System.out.println(fieldType.asPrimitiveType().getPrimitiveTypeName());
        }

      }
    });
  }
}
