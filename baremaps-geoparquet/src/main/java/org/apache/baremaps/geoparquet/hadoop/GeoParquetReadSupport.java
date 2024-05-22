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

package org.apache.baremaps.geoparquet.hadoop;

import java.util.Map;
import org.apache.baremaps.geoparquet.data.GeoParquetGroup;
import org.apache.baremaps.geoparquet.data.GeoParquetMaterializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.api.ReadSupport;
import org.apache.parquet.io.api.RecordMaterializer;
import org.apache.parquet.schema.MessageType;

public class GeoParquetReadSupport extends ReadSupport<GeoParquetGroup> {

  @Override
  public ReadContext init(
      Configuration configuration,
      Map<String, String> keyValueMetaData,
      MessageType fileSchema) {
    String partialSchemaString = configuration.get(ReadSupport.PARQUET_READ_SCHEMA);
    MessageType requestedProjection = getSchemaForRead(fileSchema, partialSchemaString);
    return new ReadContext(requestedProjection);
  }

  @Override
  public RecordMaterializer<GeoParquetGroup> prepareForRead(
      Configuration configuration,
      Map<String, String> keyValueMetaData,
      MessageType fileSchema,
      ReadContext readContext) {
    return new GeoParquetMaterializer(readContext.getRequestedSchema());
  }

}
