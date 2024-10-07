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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeoParquetMetadata {

  @JsonProperty("version")
  private String version;

  @JsonProperty("primary_column")
  private String primaryColumn;

  @JsonProperty("columns")
  private Map<String, GeoParquetColumnMetadata> columns;

  @JsonProperty("encoding")
  private String encoding;

  @JsonProperty("geometry_types")
  private List<String> geometryTypes;

  @JsonProperty("crs")
  private Object crs;

  @JsonProperty("edges")
  private String edges;

  @JsonProperty("bbox")
  private List<Double> bbox;

  @JsonProperty("epoch")
  private String epoch;

  @JsonProperty("covering")
  private Object covering;

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

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public List<String> getGeometryTypes() {
    return geometryTypes;
  }

  public void setGeometryTypes(List<String> geometryTypes) {
    this.geometryTypes = geometryTypes;
  }

  public Object getCrs() {
    return crs;
  }

  public void setCrs(Object crs) {
    this.crs = crs;
  }

  public String getEdges() {
    return edges;
  }

  public void setEdges(String edges) {
    this.edges = edges;
  }

  public List<Double> getBbox() {
    return bbox;
  }

  public void setBbox(List<Double> bbox) {
    this.bbox = bbox;
  }

  public String getEpoch() {
    return epoch;
  }

  public void setEpoch(String epoch) {
    this.epoch = epoch;
  }

  public Object getCovering() {
    return covering;
  }

  public void setCovering(Object covering) {
    this.covering = covering;
  }

  public int getSrid(String column) {
    return Optional.ofNullable(getColumns().get(column).getCrs()).map(crs -> {
      JsonNode id = crs.get("id");
      return switch (id.get("authority").asText()) {
        case "OGC" -> switch (id.get("code").asText()) {
            case "CRS84" -> 4326;
            default -> 0;
          };
        case "EPSG" -> id.get("code").asInt();
        default -> 0;
      };
    }).orElse(4326);
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
