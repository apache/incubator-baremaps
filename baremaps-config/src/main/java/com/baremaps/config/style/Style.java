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
package com.baremaps.config.style;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Style {

  private Integer version = 8;

  private String name;

  private Double bearing;

  private List<Double> center;

  private String glyphs;

  private ObjectNode light;

  private ObjectNode metadata;

  private Double pitch;

  private String sprite;

  private ObjectNode terrain;

  private ObjectNode transition;

  private Double zoom;

  private Map<String, Object> sources = new HashMap<>();

  private List<Object> layers = new ArrayList<>();

  public Integer getVersion() {
    return version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getBearing() {
    return bearing;
  }

  public void setBearing(Double bearing) {
    this.bearing = bearing;
  }

  public List<Double> getCenter() {
    return center;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setCenter(List<Double> center) {
    this.center = center;
  }

  public String getGlyphs() {
    return glyphs;
  }

  public void setGlyphs(String glyphs) {
    this.glyphs = glyphs;
  }

  public ObjectNode getLight() {
    return light;
  }

  public void setLight(ObjectNode light) {
    this.light = light;
  }

  public ObjectNode getMetadata() {
    return metadata;
  }

  public void setMetadata(ObjectNode metadata) {
    this.metadata = metadata;
  }

  public Double getPitch() {
    return pitch;
  }

  public void setPitch(Double pitch) {
    this.pitch = pitch;
  }

  public String getSprite() {
    return sprite;
  }

  public void setSprite(String sprite) {
    this.sprite = sprite;
  }

  public JsonNode getTerrain() {
    return terrain;
  }

  public void setTerrain(ObjectNode terrain) {
    this.terrain = terrain;
  }

  public ObjectNode getTransition() {
    return transition;
  }

  public void setTransition(ObjectNode transition) {
    this.transition = transition;
  }

  public Double getZoom() {
    return zoom;
  }

  public void setZoom(Double zoom) {
    this.zoom = zoom;
  }

  public Map<String, Object> getSources() {
    return sources;
  }

  public void setSources(Map<String, Object> sources) {
    this.sources = sources;
  }

  public List<Object> getLayers() {
    return layers;
  }

  public void setLayers(List<Object> layers) {
    this.layers = layers;
  }
}
