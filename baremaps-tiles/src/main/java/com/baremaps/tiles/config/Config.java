package com.baremaps.tiles.config;

import java.io.InputStream;
import java.util.List;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Config {

  private List<Layer> layers;

  public List<Layer> getLayers() {
    return layers;
  }

  public void setLayers(List<Layer> layers) {
    this.layers = layers;
  }

  public static Config load(InputStream input) {
    Yaml yaml = new Yaml(new Constructor(Config.class));
    return yaml.load(input);
  }

}
