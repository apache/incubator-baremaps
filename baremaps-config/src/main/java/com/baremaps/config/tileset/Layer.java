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

package com.baremaps.config.tileset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Layer {

  private String id;

  private String description;

  private List<Query> queries;

  public Layer() {}

  public Layer(Query... queries) {
    this.queries = Arrays.asList(queries);
  }

  public Layer(String id, Query... queries) {
    this.id = id;
    this.queries = Arrays.asList(queries);
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public List<Query> getQueries() {
    return queries;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setQueries(List<Query> queries) {
    this.queries = queries;
  }
}
