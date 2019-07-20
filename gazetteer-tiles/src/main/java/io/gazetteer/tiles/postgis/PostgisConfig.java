package io.gazetteer.tiles.postgis;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;

public class PostgisConfig {

  private List<PostgisLayer> layers;

  public List<PostgisLayer> getLayers() {
    return layers;
  }

  public void setLayers(List<PostgisLayer> layers) {
    this.layers = layers;
  }

  public static PostgisConfig load(InputStream input) {
    Yaml yaml = new Yaml(new Constructor(PostgisConfig.class));
    return yaml.load(input);
  }
}
