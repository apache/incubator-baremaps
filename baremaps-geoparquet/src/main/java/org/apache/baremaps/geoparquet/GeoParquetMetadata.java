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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record GeoParquetMetadata(
    @JsonProperty("version") String version,
    @JsonProperty("primary_column") String primaryColumn,
    @JsonProperty("columns") Map<String, Column> columns,
    @JsonProperty("encoding") String encoding,
    @JsonProperty("geometry_types") List<String> geometryTypes,
    @JsonProperty("crs") Object crs,
    @JsonProperty("edges") String edges,
    @JsonProperty("bbox") List<Double> bbox,
    @JsonProperty("epoch") String epoch,
    @JsonProperty("covering") Object covering) {

  public int getSrid(String column) {
    return Optional.ofNullable(columns.get(column).crs()).map(crs -> {
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

  public record Column(
      @JsonProperty("encoding") String encoding,
      @JsonProperty("geometry_types") List<String> geometryTypes,
      @JsonProperty("crs") JsonNode crs,
      @JsonProperty("orientation") String orientation,
      @JsonProperty("edges") String edges,
      @JsonProperty("bbox") Double[] bbox) {
  }
}
