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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Tileset {

  private String tilejson = "2.1.0";

  private String id = "baremaps";

  private String name = "baremaps";

  private String version = "0.0.1";

  private String format = "mvt";

  private String description = "baremaps";

  private String attribution = "baremaps";

  private Center center = new Center();

  private Bounds bounds = new Bounds();

  private List<String> tiles = Lists.newArrayList("http://localhost:9001/tiles/{z}/{x}/{y}.mvt");

  private double minZoom = 0;

  private double maxZoom = 20;

  @JsonProperty("vector_layers")
  private List<Layer> layers = Lists.newArrayList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAttribution() {
    return attribution;
  }

  public void setAttribution(String attribution) {
    this.attribution = attribution;
  }

  public Center getCenter() {
    return center;
  }

  public void setCenter(Center center) {
    this.center = center;
  }

  public Bounds getBounds() {
    return bounds;
  }

  public void setBounds(Bounds bounds) {
    this.bounds = bounds;
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

  public List<Layer> getLayers() {
    return layers;
  }

  public void setLayers(List<Layer> layers) {
    this.layers = layers;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getTilejson() {
    return tilejson;
  }

  public void setTilejson(String tilejson) {
    this.tilejson = tilejson;
  }

  public List<String> getTiles() {
    return tiles;
  }

  public void setTiles(List<String> tiles) {
    this.tiles = tiles;
  }
}
