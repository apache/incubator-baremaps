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
import java.util.Map;

public class Config {

  private String id = "baremaps";

  private String host = "localhost";

  private int port = 9000;

  private double lon = 0;

  private double lat = 0;

  private double zoom = 0;

  private double minZoom = 0;

  private double maxZoom = 22;

  private double bearing = 0;

  private double pitch = 0;

  private List<Layer> layers;

  private List<Map<String, Object>> styles;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getZoom() {
    return zoom;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
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

  public double getBearing() {
    return bearing;
  }

  public void setBearing(double bearing) {
    this.bearing = bearing;
  }

  public double getPitch() {
    return pitch;
  }

  public void setPitch(double pitch) {
    this.pitch = pitch;
  }

  public List<Layer> getLayers() {
    return layers;
  }

  public void setLayers(List<Layer> layers) {
    this.layers = layers;
  }

  public List<Map<String, Object>> getStyles() {
    return styles;
  }

  public void setStyles(List<Map<String, Object>> styles) {
    this.styles = styles;
  }

}
