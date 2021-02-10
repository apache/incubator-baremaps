package com.baremaps.config.style;

import com.google.common.collect.Lists;
import java.util.List;

public class StyleSheet {

  private String id = "";

  private List<StyleLayer> layers = Lists.newArrayList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<StyleLayer> getLayers() {
    return layers;
  }

  public void setLayers(List<StyleLayer> styles) {
    this.layers = styles;
  }

}
