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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class Bounds {

  private Double minLon = -180d;

  private Double minLat = -85.05112877980659d;

  private Double maxLon = 180d;

  private Double maxLat = 85.0511287798066d;

  public Double getMinLon() {
    return minLon;
  }

  public void setMinLon(Double minLon) {
    this.minLon = minLon;
  }

  public Double getMinLat() {
    return minLat;
  }

  public void setMinLat(Double minLat) {
    this.minLat = minLat;
  }

  public Double getMaxLon() {
    return maxLon;
  }

  public void setMaxLon(Double maxLon) {
    this.maxLon = maxLon;
  }

  public Double getMaxLat() {
    return maxLat;
  }

  public void setMaxLat(Double maxLat) {
    this.maxLat = maxLat;
  }

  public List<Double> asList() {
    return List.of(minLon, minLat, maxLon, maxLat);
  }
}
