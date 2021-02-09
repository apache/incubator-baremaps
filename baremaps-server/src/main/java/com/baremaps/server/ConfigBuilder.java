/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.server;

import com.baremaps.config.legacy.Config;
import com.baremaps.config.legacy.Layer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigBuilder {

  private final Config config;

  public ConfigBuilder(Config config) {
    this.config = config;
  }

  public Map<String, Object> format() {
    // Order the config properties
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", config.getId());

    // Group the properties used by the blueprint
    map.put("center", config.getCenter());
    map.put("bounds", config.getBounds());
    map.put("server", config.getServer());

    // Put the nested properties at the end
    map.put("layers", formatLayers(config.getLayers()));

    // Put the nested properties at the end
    map.put("stylesheets", formatLayers(config.getLayers()));

    return map;
  }

  private List<Map<String, Object>> formatLayers(List<Layer> layers) {
    return layers.stream()
        .sorted(Comparator.comparing(Layer::getId))
        .map(this::formatLayer)
        .collect(Collectors.toList());
  }

  private Map<String, Object> formatLayer(Layer layer) {
    // Order the layer properties
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("name", layer.getId());
    map.put("queries", layer.getQueries());
    return map;
  }

}
