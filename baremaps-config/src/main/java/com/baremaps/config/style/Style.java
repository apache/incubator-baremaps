package com.baremaps.config.style;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Style {

  private final Integer version = 8;

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

  private boolean reload = false;

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

  public boolean isReload() {
    return reload;
  }

  public void setReload(boolean reload) {
    this.reload = reload;
  }
}
