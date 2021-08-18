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

public class Query {

  private int minzoom;

  private int maxzoom;

  private String sql;

  public Query() {

  }

  public Query(int minzoom, int maxzoom, String sql) {
    this.minzoom = minzoom;
    this.maxzoom = maxzoom;
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }

  public int getMinzoom() {
    return minzoom;
  }

  public int getMaxzoom() {
    return maxzoom;
  }

}
