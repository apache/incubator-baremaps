package com.baremaps.core.config;

import com.baremaps.core.config.Source;
import java.util.List;

public class Config {

  private List<Source> sources;

  public Config() {

  }

  public List<Source> getSources() {
    return sources;
  }

  public void setSources(List<Source> sources) {
    this.sources = sources;
  }

}
