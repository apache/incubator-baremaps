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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Tileset {

  private String tilejson = "2.2.0";

  private String name;

  private String version;

  private String description;

  private String attribution;

  private Center center;

  private Bounds bounds = new Bounds();

  private double minzoom;

  private double maxzoom;

  private List<String> tiles = new ArrayList<>();

  @JsonProperty("vector_layers")
  private List<Layer> layers = new ArrayList<>();

  public Tileset() {

  }

  public Tileset(Layer... vectorLayers) {
    this.vectorLayers = Arrays.asList(vectorLayers);
  }

  public Tileset(String name, Layer... vectorLayers) {
    this.name = name;
    this.vectorLayers = Arrays.asList(vectorLayers);
  }

  @JsonProperty("vector_layers")
  private List<Layer> vectorLayers = new ArrayList<>();

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

  public double getMinzoom() {
    return minzoom;
  }

  public void setMinzoom(double minzoom) {
    this.minzoom = minzoom;
  }

  public double getMaxzoom() {
    return maxzoom;
  }

  public void setMaxzoom(double maxzoom) {
    this.maxzoom = maxzoom;
  }

  public List<Layer> getVectorLayers() {
    return vectorLayers;
  }

  public void setVectorLayers(List<Layer> vectorLayers) {
    this.vectorLayers = vectorLayers;
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
