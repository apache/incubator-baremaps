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
import com.baremaps.config.style.StyleLayer;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlueprintMapper implements Function<Source, Map<String, Object>> {

  @Override
  public Map<String, Object> apply(Source source) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", "baremaps");
    map.put("version", 8);
    map.put("sprite", "");
    map.put("glyphs", "https://daglvzoa8byzd.cloudfront.net/{fontstack}/{range}.pbf");

    map.put("sources", ImmutableSortedMap.naturalOrder()
        .put("baremaps", ImmutableSortedMap.naturalOrder()
            .put("type", "vector")
            .put("minZoom", source.getBounds().getMinZoom())
            .put("maxZoom", source.getBounds().getMaxZoom())
            .put("bounds", new double[]{
                source.getBounds().getMinLon(), source.getBounds().getMinLat(),
                source.getBounds().getMaxLon(), source.getBounds().getMaxLat()})
            .put("tiles", Arrays.asList(String.format("http://%s:%s/tiles/{z}/{x}/{y}.pbf",
                source.getServer().getHost(),
                source.getServer().getPort())))
            .build())
        .build());

    List styles = styles(source).map(style -> {
          Map<String, Object> layer = new TreeMap<>();
          layer.put("id", style.getId());
          layer.put("source", Optional.ofNullable(style.getSource()).orElse("baremaps"));
          layer.put("source-layer", style.getSourceLayer());
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

  private Stream<StyleLayer> styles(Source source) {
      List<StyleLayer> styles = new ArrayList<>();
      styles.add(background());
      for (SourceLayer layer : source.getLayers()) {
        styles.addAll(layerBlueprint(layer));
      }
      return styles.stream();

  }

  private StyleLayer background() {
    StyleLayer style = new StyleLayer();
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

  private List<StyleLayer> layerBlueprint(SourceLayer layer) {
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

  private StyleLayer pointBlueprint(SourceLayer sourceLayer, String id) {
    StyleLayer styleLayer = new StyleLayer();
    styleLayer.setId(id);
    styleLayer.setSourceLayer(sourceLayer.getId());
    styleLayer.setMinZoom(sourceLayer.getQueries().stream().mapToInt(q -> q.getMinZoom()).min().getAsInt());
    styleLayer.setMaxZoom(sourceLayer.getQueries().stream().mapToInt(q -> q.getMaxZoom()).max().getAsInt());
    styleLayer.setType("circle");
    styleLayer.setLayout(ImmutableSortedMap.naturalOrder()
        .put("visibility", "visible")
        .build());
    styleLayer.setPaint(ImmutableSortedMap.naturalOrder()
        .put("circle-color", "rgba(229, 235, 247, 0.75)")
        .put("circle-radius", 1)
        .build());
    return styleLayer;
  }

  private StyleLayer linestringBlueprint(SourceLayer sourceLayer, String id) {
    StyleLayer styleLayer = new StyleLayer();
    styleLayer.setId(id);
    styleLayer.setSourceLayer(sourceLayer.getId());
    styleLayer.setMinZoom(sourceLayer.getQueries().stream().mapToInt(q -> q.getMinZoom()).min().getAsInt());
    styleLayer.setMaxZoom(sourceLayer.getQueries().stream().mapToInt(q -> q.getMaxZoom()).max().getAsInt());
    styleLayer.setType("line");
    styleLayer.setLayout(ImmutableSortedMap.naturalOrder()
        .put("visibility", "visible")
        .build());
    styleLayer.setPaint(ImmutableSortedMap.naturalOrder()
        .put("line-color", "rgba(229, 235, 247, 0.50)")
        .put("line-width", 1)
        .build());
    return styleLayer;
  }

  private StyleLayer polygonBlueprint(SourceLayer sourceLayer, String id) {
    StyleLayer styleLayer = new StyleLayer();
    styleLayer.setId(id);
    styleLayer.setSourceLayer(sourceLayer.getId());
    styleLayer.setMinZoom(sourceLayer.getQueries().stream().mapToInt(q -> q.getMinZoom()).min().getAsInt());
    styleLayer.setMaxZoom(sourceLayer.getQueries().stream().mapToInt(q -> q.getMaxZoom()).max().getAsInt());
    styleLayer.setType("fill");
    styleLayer.setLayout(ImmutableSortedMap.naturalOrder()
        .put("visibility", "visible")
        .build());
    styleLayer.setPaint(ImmutableSortedMap.naturalOrder()
        .put("fill-outline-color", "rgba(229, 235, 247, 0.50)")
        .put("fill-color", "rgba(229, 235, 247, 0.25)")
        .put("fill-antialias", true)
        .build());
    return styleLayer;
  }


}
