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
import java.util.List;

public class GeoParquetColumnMetadata {

  @JsonProperty("encoding")
  private String encoding;

  @JsonProperty("geometry_types")
  private List<String> geometryTypes;

  @JsonProperty("crs")
  private JsonNode crs;

  @JsonProperty("orientation")
  private String orientation;

  @JsonProperty("edges")
  private String edges;

  @JsonProperty("bbox")
  private Double[] bbox;

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

  public JsonNode getCrs() {
    return crs;
  }

  public void setCrs(JsonNode crs) {
    this.crs = crs;
  }

  public String getOrientation() {
    return orientation;
  }

  public void setOrientation(String orientation) {
    this.orientation = orientation;
  }

  public String getEdges() {
    return edges;
  }

  public void setEdges(String edges) {
    this.edges = edges;
  }

  public Double[] getBbox() {
    return bbox;
  }

  public void setBbox(Double[] bbox) {
    this.bbox = bbox;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeoParquetColumnMetadata that = (GeoParquetColumnMetadata) o;
    return Objects.equal(encoding, that.encoding)
        && Objects.equal(geometryTypes, that.geometryTypes)
        && Objects.equal(crs, that.crs)
        && Objects.equal(orientation, that.orientation)
        && Objects.equal(edges, that.edges)
        && Objects.equal(bbox, that.bbox);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(encoding, geometryTypes, crs, orientation, edges, bbox);
  }
}
