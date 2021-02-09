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
package com.baremaps.config.legacy;

public class Bounds {

  private double minLon = -180;

  private double maxLon = 180;

  private double minLat = -85.05113;

  private double maxLat = 85.05113;

  private double minZoom = 0;

  private double maxZoom = 20;

  public double getMinLon() {
    return minLon;
  }

  public void setMinLon(double minLon) {
    this.minLon = minLon;
  }

  public double getMaxLon() {
    return maxLon;
  }

  public void setMaxLon(double maxLon) {
    this.maxLon = maxLon;
  }

  public double getMinLat() {
    return minLat;
  }

  public void setMinLat(double minLat) {
    this.minLat = minLat;
  }

  public double getMaxLat() {
    return maxLat;
  }

  public void setMaxLat(double maxLat) {
    this.maxLat = maxLat;
  }

  public double getMinZoom() {
    return minZoom;
  }

  public void setMinZoom(double minZoom) {
    this.minZoom = minZoom;
  }

  public double getMaxZoom() {
    return maxZoom;
  }

  public void setMaxZoom(double maxZoom) {
    this.maxZoom = maxZoom;
  }

}
