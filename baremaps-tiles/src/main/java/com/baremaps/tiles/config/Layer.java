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
