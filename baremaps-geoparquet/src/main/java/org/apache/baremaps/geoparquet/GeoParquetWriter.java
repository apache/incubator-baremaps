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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.conf.ParquetConfiguration;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.schema.MessageType;

/**
 * A writer for GeoParquet files that writes GeoParquetGroup instances to a Parquet file.
 */
public class GeoParquetWriter {

  private GeoParquetWriter() {
    // Prevent instantiation
  }

  public static Builder builder(Path file) {
    return new Builder(file);
  }

  public static class Builder
      extends ParquetWriter.Builder<GeoParquetGroup, GeoParquetWriter.Builder> {

    private MessageType type = null;

    private GeoParquetMetadata metadata = null;

    private Builder(Path file) {
      super(file);
    }

    public GeoParquetWriter.Builder withType(MessageType type) {
      this.type = type;
      return this;
    }

    public GeoParquetWriter.Builder withGeoParquetMetadata(GeoParquetMetadata metadata) {
      this.metadata = metadata;
      return this;
    }

    @Override
    protected WriteSupport<GeoParquetGroup> getWriteSupport(Configuration conf) {
      return getWriteSupport((ParquetConfiguration) null);
    }

    @Override
    protected WriteSupport<GeoParquetGroup> getWriteSupport(ParquetConfiguration conf) {
      return new GeoParquetWriteSupport(type, metadata);
    }

    @Override
    protected GeoParquetWriter.Builder self() {
      return this;
    }
  }
}
