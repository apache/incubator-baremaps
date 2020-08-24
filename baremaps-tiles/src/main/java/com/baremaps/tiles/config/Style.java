package com.baremaps.tiles.config;

import java.util.List;

public class Style {

  private String id;

  private String type;

  private String metadata;

  private int minZoom;

  private int maxZoom;

  private String layer;

  private List<Object> filter;

  private Object layout;

  private Object paint;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public int getMinZoom() {
    return minZoom;
  }

  public void setMinZoom(int minZoom) {
    this.minZoom = minZoom;
  }

  public int getMaxZoom() {
    return maxZoom;
  }

  public void setMaxZoom(int maxZoom) {
    this.maxZoom = maxZoom;
  }

  public String getLayer() {
    return layer;
  }

  public void setLayer(String layer) {
    this.layer = layer;
  }

  public List<Object> getFilter() {
    return filter;
  }

  public void setFilter(List<Object> filter) {
    this.filter = filter;
  }

  public Object getLayout() {
    return layout;
  }

  public void setLayout(Object layout) {
    this.layout = layout;
  }

  public Object getPaint() {
    return paint;
  }

  public void setPaint(Object paint) {
    this.paint = paint;
  }

}
