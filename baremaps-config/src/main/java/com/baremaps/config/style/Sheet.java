package com.baremaps.config.style;

import com.google.common.collect.Lists;
import java.util.List;

public class Sheet {

  private String id = "";

  private List<Layer> layers = Lists.newArrayList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Layer> getLayers() {
    return layers;
  }

  public void setLayers(List<Layer> styles) {
    this.layers = styles;
  }

}
