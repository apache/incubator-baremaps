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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import java.util.Map;

public class GeoParquetMetadata {

  @JsonProperty("version")
  private String version;

  @JsonProperty("primary_column")
  private String primaryColumn;

  @JsonProperty("columns")
  private Map<String, GeoParquetColumnMetadata> columns;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getPrimaryColumn() {
    return primaryColumn;
  }

  public void setPrimaryColumn(String primaryColumn) {
    this.primaryColumn = primaryColumn;
  }

  public Map<String, GeoParquetColumnMetadata> getColumns() {
    return columns;
  }

  public void setColumns(Map<String, GeoParquetColumnMetadata> columns) {
    this.columns = columns;
  }

  public int getSrid(String column) {
    JsonNode crsId = getColumns().get(column).getCrs().get("id");
    return switch (crsId.get("authority").asText()) {
      case "OGC" -> switch (crsId.get("code").asText()) {
          case "CRS84" -> 4326;
          default -> 0;
        };
      case "EPSG" -> crsId.get("code").asInt();
      default -> 0;
    };
  }

  public boolean isGeometryColumn(String column) {
    return columns.containsKey(column);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeoParquetMetadata that = (GeoParquetMetadata) o;
    return Objects.equal(version, that.version)
        && Objects.equal(primaryColumn, that.primaryColumn)
        && Objects.equal(columns, that.columns);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(version, primaryColumn, columns);
  }
}
