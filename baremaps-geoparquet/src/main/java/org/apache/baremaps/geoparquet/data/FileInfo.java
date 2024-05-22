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

import com.google.common.base.Objects;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;

public final class FileInfo {

  private final long rowCount;

  private final ParquetMetadata parquetMetadata;

  private final GeoParquetMetadata geoParquetMetadata;

  public FileInfo(
      long rowCount,
      ParquetMetadata parquetMetadata,
      GeoParquetMetadata geoParquetMetadata) {
    this.rowCount = rowCount;
    this.parquetMetadata = parquetMetadata;
    this.geoParquetMetadata = geoParquetMetadata;
  }

  public long getRowCount() {
    return rowCount;
  }

  public ParquetMetadata getParquetMetadata() {
    return parquetMetadata;
  }

  public GeoParquetMetadata getGeoParquetMetadata() {
    return geoParquetMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileInfo that = (FileInfo) o;
    return rowCount == that.rowCount
        && Objects.equal(parquetMetadata, that.parquetMetadata)
        && Objects.equal(geoParquetMetadata, that.geoParquetMetadata);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(rowCount, parquetMetadata, geoParquetMetadata);
  }
}
