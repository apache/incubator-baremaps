/*
 * Copyright (C) 2011 The Baremaps Authors
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

import java.util.List;

public class Layer {

  private String name;

  private int minZoom;

  private int maxZoom;

  private List<String> queries;

  public Layer() {

  }

  public Layer(String name, String geometry, int minZoom, int maxZoom, List<String> queries) {
    this.name = name;
    this.minZoom = minZoom;
    this.maxZoom = maxZoom;
    this.queries = queries;
  }

  public String getName() {
    return name;
  }

  public int getMinZoom() {
    return minZoom;
  }

  public int getMaxZoom() {
    return maxZoom;
  }

  public List<String> getQueries() {
    return queries;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMinZoom(int minZoom) {
    this.minZoom = minZoom;
  }

  public void setMaxZoom(int maxZoom) {
    this.maxZoom = maxZoom;
  }

  public void setQueries(List<String> queries) {
    this.queries = queries;
  }
}
