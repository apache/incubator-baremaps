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

package org.apache.baremaps.geoparquet.data;

import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.io.api.RecordMaterializer;
import org.apache.parquet.schema.MessageType;

public class GeoParquetMaterializer extends RecordMaterializer<GeoParquetGroupImpl> {

  private final GeoParquetGroupFactory groupFactory;

  private final GeoParquetGroupConverter root;

  public GeoParquetMaterializer(MessageType schema, GeoParquetMetadata metadata) {
    this.groupFactory = new GeoParquetGroupFactory(schema, metadata);
    this.root = new GeoParquetGroupConverter(null, 0, schema) {
      @Override
      public void start() {
        this.current = groupFactory.newGroup();
      }
    };
  }

  @Override
  public GeoParquetGroupImpl getCurrentRecord() {
    return root.getCurrentRecord();
  }

  @Override
  public GroupConverter getRootConverter() {
    return root;
  }

}
