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
import java.util.Set;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;

public final class GeoParquetFileInfo {

  private final long rowCount;
  private final ParquetMetadata parquetMetadata;
  private final GeoParquetMetadata geoParquetMetadata;
  private final Set<String> geometryColumns;

  public GeoParquetFileInfo(
      long rowCount,
      ParquetMetadata parquetMetadata,
      GeoParquetMetadata geoParquetMetadata,
      Set<String> geometryColumns) {
    this.rowCount = rowCount;
    this.parquetMetadata = parquetMetadata;
    this.geoParquetMetadata = geoParquetMetadata;
    this.geometryColumns = geometryColumns;
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

  public Set<String> getGeometryColumns() {
    return geometryColumns;
  }

  public boolean isGeometryColumn(String column) {
    return geometryColumns.contains(column);
  }

  public boolean isGeometryColumn(int column) {
    return isGeometryColumn(
        parquetMetadata.getFileMetaData().getSchema().getColumns().get(column).getPath()[0]);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeoParquetFileInfo that = (GeoParquetFileInfo) o;
    return rowCount == that.rowCount
        && Objects.equal(parquetMetadata, that.parquetMetadata)
        && Objects.equal(geoParquetMetadata, that.geoParquetMetadata)
        && Objects.equal(geometryColumns, that.geometryColumns);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(rowCount, parquetMetadata, geoParquetMetadata, geometryColumns);
  }
}
