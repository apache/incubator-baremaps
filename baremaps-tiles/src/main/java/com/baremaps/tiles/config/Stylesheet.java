package com.baremaps.tiles.config;

import com.google.common.collect.Lists;
import java.util.List;

public class Stylesheet {

  private String id;

  private List<Style> styles = Lists.newArrayList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Style> getStyles() {
    return styles;
  }

  public void setStyles(List<Style> styles) {
    this.styles = styles;
  }

}
