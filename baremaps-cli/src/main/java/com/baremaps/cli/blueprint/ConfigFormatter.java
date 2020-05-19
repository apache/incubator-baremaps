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
package com.baremaps.cli.blueprint;

import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.config.Layer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigFormatter {

  private final Config config;

  public ConfigFormatter(Config config) {
    this.config = config;
  }

  public Map<String, Object> format() {
    // Order the config properties
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", config.getId());
    map.put("host", config.getHost());
    map.put("port", config.getPort());

    // Group the properties used by the blueprint
    map.put("center", config.getCenter());
    map.put("bounds", config.getBounds());

    // Put the nested properties at the end
    map.put("layers", formatLayers(config.getLayers()));
    map.put("styles", formatStyles(config.getStyles()));

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
    map.put("type", layer.getType());
    map.put("minZoom", layer.getMinZoom());
    map.put("maxZoom", layer.getMaxZoom());
    map.put("queries", layer.getQueries());
    return map;
  }

  private List<Map<String, Object>> formatStyles(List<Map<String, Object>> styles) {
    if (config.getStyles() == null) {
      BlueprintBuilder builder = new BlueprintBuilder(config);
      return builder.buildStyles();
    } else {
      return styles.stream()
          .map(style -> formatStyle(style))
          .collect(Collectors.toList());
    }
  }

  private Map<String, Object> formatStyle(Map<String, Object> style) {
    // Order the style properties
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", style.get("id"));
    map.put("source", style.get("source"));
    map.put("source-layer", style.get("source-layer"));
    map.put("minZoom", style.get("minZoom"));
    map.put("maxZoom", style.get("maxZoom"));
    map.put("type", style.get("type"));
    map.putAll(style);
    return map;
  }

}
