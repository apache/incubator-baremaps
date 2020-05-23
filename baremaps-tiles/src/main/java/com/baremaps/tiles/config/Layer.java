/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.tiles.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Layer {

  private String id;

  private String type;

  private String description;

  private int minZoom;

  private int maxZoom;

  private Map<String, String> fields = new HashMap<>();

  private List<String> queries;

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public int getMinZoom() {
    return minZoom;
  }

  public int getMaxZoom() {
    return maxZoom;
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public List<String> getQueries() {
    return queries;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setMinZoom(int minZoom) {
    this.minZoom = minZoom;
  }

  public void setMaxZoom(int maxZoom) {
    this.maxZoom = maxZoom;
  }

  public void setFields(Map<String, String> fields) {
    this.fields = fields;
  }

  public void setQueries(List<String> queries) {
    this.queries = queries;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
