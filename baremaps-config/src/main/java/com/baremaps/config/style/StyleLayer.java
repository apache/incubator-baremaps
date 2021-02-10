package com.baremaps.config.style;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StyleLayer {

  private String id;

  private String type;

  private String metadata;

  private Integer minZoom;

  private Integer maxZoom;

  private String source;

  @JsonProperty("source-layer")
  private String sourceLayer;

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

  public Integer getMinZoom() {
    return minZoom;
  }

  public void setMinZoom(int minZoom) {
    this.minZoom = minZoom;
  }

  public Integer getMaxZoom() {
    return maxZoom;
  }

  public void setMaxZoom(int maxZoom) {
    this.maxZoom = maxZoom;
  }

  public String getSourceLayer() {
    return sourceLayer;
  }

  public void setSourceLayer(String sourceLayer) {
    this.sourceLayer = sourceLayer;
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

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
