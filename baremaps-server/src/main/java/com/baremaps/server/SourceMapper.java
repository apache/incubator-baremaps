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

import com.baremaps.config.source.Source;
import com.baremaps.config.source.SourceLayer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SourceMapper implements Function<Source, Map<String, Object>> {

  @Override
  public Map<String, Object> apply(Source source) {
    // Order the config properties
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", source.getId());

    // Group the properties used by the blueprint
    map.put("center", source.getCenter());
    map.put("bounds", source.getBounds());
    map.put("server", source.getServer());

    // Put the nested properties at the end
    map.put("layers", formatLayers(source.getLayers()));

    return map;
  }

  private List<Map<String, Object>> formatLayers(List<SourceLayer> layers) {
    return layers.stream()
        .sorted(Comparator.comparing(SourceLayer::getId))
        .map(this::formatLayer)
        .collect(Collectors.toList());
  }

  private Map<String, Object> formatLayer(SourceLayer layer) {
    // Order the layer properties
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("name", layer.getId());
    map.put("queries", layer.getQueries());
    return map;
  }

}
