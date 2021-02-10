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

import com.baremaps.config.style.Style;
import com.baremaps.config.style.StyleLayer;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StyleMapper implements Function<Style, Map<String, Object>> {

  @Override
  public Map<String, Object> apply(Style style) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", "baremaps");
    map.put("version", 8);
    map.put("sprite", "");
    map.put("glyphs", "https://daglvzoa8byzd.cloudfront.net/{fontstack}/{range}.pbf");

    map.put("sources", ImmutableSortedMap.naturalOrder()
        .put("baremaps", ImmutableSortedMap.naturalOrder()
            .put("type", "vector")
            .put("minZoom", style.getBounds().getMinZoom())
            .put("maxZoom", style.getBounds().getMaxZoom())
            .put("bounds", new double[]{
                style.getBounds().getMinLon(), style.getBounds().getMinLat(),
                style.getBounds().getMaxLon(), style.getBounds().getMaxLat()})
            .put("tiles", Arrays.asList(String.format("http://%s:%s/tiles/{z}/{x}/{y}.pbf",
                style.getServer().getHost(),
                style.getServer().getPort())))
            .build())
        .build());

    List layers = layers(style).stream().map(layer -> {
      Map<String, Object> attributes = new TreeMap<>();
      attributes.put("id", layer.getId());
      attributes.put("source", Optional.ofNullable(layer.getSource()).orElse("baremaps"));
      attributes.put("source-layer", layer.getSourceLayer());
      attributes.put("type", layer.getType());
      attributes.put("minzoom", layer.getMinZoom());
      attributes.put("maxzoom", layer.getMaxZoom());
      attributes.put("layout", layer.getLayout());
      attributes.put("metadata", layer.getMetadata());
      attributes.put("filter", layer.getFilter());
      attributes.put("paint", layer.getPaint());
      return layer;
    }).collect(Collectors.toList());

    map.put("layers", layers);

    return map;
  }

  private List<StyleLayer> layers(Style style) {
    List<StyleLayer> styles = style.getSheets().stream()
        .flatMap(sheet -> sheet.getLayers().stream())
        .collect(Collectors.toList());
    return Lists.reverse(styles);
  }



}