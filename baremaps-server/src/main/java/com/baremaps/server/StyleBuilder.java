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
import com.baremaps.config.legacy.Style;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StyleBuilder {

  private final Config config;

  public StyleBuilder(Config config) {
    this.config = config;
  }

  public Map<String, Object> build() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", "baremaps");
    map.put("version", 8);
    map.put("sprite", "");
    map.put("glyphs", "https://daglvzoa8byzd.cloudfront.net/{fontstack}/{range}.pbf");

    map.put("sources", ImmutableSortedMap.naturalOrder()
        .put("baremaps", ImmutableSortedMap.naturalOrder()
            .put("type", "vector")
            .put("minZoom", config.getBounds().getMinZoom())
            .put("maxZoom", config.getBounds().getMaxZoom())
            .put("bounds", new double[]{
                config.getBounds().getMinLon(), config.getBounds().getMinLat(),
                config.getBounds().getMaxLon(), config.getBounds().getMaxLat()})
            .put("tiles", Arrays.asList(String.format("http://%s:%s/tiles/{z}/{x}/{y}.pbf",
                config.getServer().getHost(),
                config.getServer().getPort())))
            .build())
        .build());

    List styles = styles().stream().map(style -> {
          Map<String, Object> layer = new TreeMap<>();
          layer.put("id", style.getId());
          layer.put("source", Optional.ofNullable(style.getSource()).orElse("baremaps"));
          layer.put("source-layer", style.getLayer());
          layer.put("type", style.getType());
          layer.put("minzoom", style.getMinZoom());
          layer.put("maxzoom", style.getMaxZoom());
          layer.put("layout", style.getLayout());
          layer.put("metadata", style.getMetadata());
          layer.put("filter", style.getFilter());
          layer.put("paint", style.getPaint());
          return layer;
        }
    ).collect(Collectors.toList());

    map.put("layers", styles);

    return map;
  }

  private List<Style> styles() {
    if (config.getStylesheets().isEmpty()) {
      List<Style> styles = new ArrayList<>();
      styles.add(background());
      for (Layer layer : config.getLayers()) {
        styles.addAll(layerBlueprint(layer));
      }
      return styles;
    } else {
      List<Style> styles = config.getStylesheets().stream()
          .flatMap(component -> component.getStyles().stream())
          .collect(Collectors.toList());
      return Lists.reverse(styles);
    }
  }

  private Style background() {
    Style style = new Style();
    style.setId("background");
    style.setType("background");
    style.setLayout(ImmutableSortedMap.naturalOrder()
        .put("visibility", "visible")
        .build());
    style.setPaint(ImmutableSortedMap.naturalOrder()
        .put("background-color", "rgb(64, 92, 176)")
        .build());
    return style;
  }

  private List<Style> layerBlueprint(Layer layer) {
    switch (layer.getType()) {
      case "point":
        return Arrays.asList(pointBlueprint(layer, String.format("%s_point", layer.getId())));
      case "line":
        return Arrays.asList(linestringBlueprint(layer, String.format("%s_linestring", layer.getId())));
      case "polygon":
        return Arrays.asList(polygonBlueprint(layer, String.format("%s_polygon", layer.getId())));
      case "geometry":
        return Arrays.asList(
            pointBlueprint(layer, String.format("%s_point", layer.getId())),
            linestringBlueprint(layer, String.format("%s_linestring", layer.getId())));
      default:
        return Arrays.asList();
    }
  }

  private Style pointBlueprint(Layer layer, String id) {
    Style style = new Style();
    style.setId(id);
    style.setLayer(layer.getId());
    style.setMinZoom(layer.getQueries().stream().mapToInt(q -> q.getMinZoom()).min().getAsInt());
    style.setMaxZoom(layer.getQueries().stream().mapToInt(q -> q.getMaxZoom()).max().getAsInt());
    style.setType("circle");
    style.setLayout(ImmutableSortedMap.naturalOrder()
        .put("visibility", "visible")
        .build());
    style.setPaint(ImmutableSortedMap.naturalOrder()
        .put("circle-color", "rgba(229, 235, 247, 0.75)")
        .put("circle-radius", 1)
        .build());
    return style;
  }

  private Style linestringBlueprint(Layer layer, String id) {
    Style style = new Style();
    style.setId(id);
    style.setLayer(layer.getId());
    style.setMinZoom(layer.getQueries().stream().mapToInt(q -> q.getMinZoom()).min().getAsInt());
    style.setMaxZoom(layer.getQueries().stream().mapToInt(q -> q.getMaxZoom()).max().getAsInt());
    style.setType("line");
    style.setLayout(ImmutableSortedMap.naturalOrder()
        .put("visibility", "visible")
        .build());
    style.setPaint(ImmutableSortedMap.naturalOrder()
        .put("line-color", "rgba(229, 235, 247, 0.50)")
        .put("line-width", 1)
        .build());
    return style;
  }

  private Style polygonBlueprint(Layer layer, String id) {
    Style style = new Style();
    style.setId(id);
    style.setLayer(layer.getId());
    style.setMinZoom(layer.getQueries().stream().mapToInt(q -> q.getMinZoom()).min().getAsInt());
    style.setMaxZoom(layer.getQueries().stream().mapToInt(q -> q.getMaxZoom()).max().getAsInt());
    style.setType("fill");
    style.setLayout(ImmutableSortedMap.naturalOrder()
        .put("visibility", "visible")
        .build());
    style.setPaint(ImmutableSortedMap.naturalOrder()
        .put("fill-outline-color", "rgba(229, 235, 247, 0.50)")
        .put("fill-color", "rgba(229, 235, 247, 0.25)")
        .put("fill-antialias", true)
        .build());
    return style;
  }

}
