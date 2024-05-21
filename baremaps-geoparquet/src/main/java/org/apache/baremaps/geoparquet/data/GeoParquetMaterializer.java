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

public class GeoParquetMaterializer extends RecordMaterializer<FeatureGroup> {

  private final FeatureGroupFactory featureGroupFactory;

  private FeatureGroupConverter root;

  public GeoParquetMaterializer(GeoParquetFileInfo fileInfo) {
    MessageType schema = fileInfo.parquetMetadata().getFileMetaData().getSchema();
    this.featureGroupFactory = new FeatureGroupFactory(fileInfo, schema);
    this.root = new FeatureGroupConverter(fileInfo, null, 0, schema) {
      @Override
      public void start() {
        this.current = featureGroupFactory.newFeatureGroup();
      }

      @Override
      public void end() {}
    };
  }

  @Override
  public FeatureGroup getCurrentRecord() {
    return root.getCurrentRecord();
  }

  @Override
  public GroupConverter getRootConverter() {
    return root;
  }

}
