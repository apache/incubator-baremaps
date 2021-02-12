package com.baremaps.server;

import com.baremaps.config.Config;
import com.baremaps.config.Style;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StyleMapper implements Function<Config, Object> {

  @Override
  public Object apply(Config source) {
    return ImmutableSortedMap.naturalOrder()
        .put("id", source.getId())
        .put("version", 8)
        .put("sprite", source.getSprite())
        .put("glyphs", source.getGlyphs())
        .put("sources", ImmutableSortedMap.naturalOrder()
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
                .build()).build())
        .put("layers", Lists.reverse(
            source.getStylesheets().stream()
                .flatMap(stylesheet -> stylesheet.getStyles().stream())
                .map(layer -> {
                  Map<String, Object> map = new TreeMap<>();
                  map.put("id", layer.getId());
                  map.put("source", Optional.ofNullable(layer.getSource()).orElse("baremaps"));
                  map.put("source-layer", layer.getLayer());
                  map.put("type", layer.getType());
                  map.put("minzoom", layer.getMinZoom());
                  map.put("maxzoom", layer.getMaxZoom());
                  map.put("layout", layer.getLayout());
                  map.put("metadata", layer.getMetadata());
                  map.put("filter", layer.getFilter());
                  map.put("paint", layer.getPaint());
                  return map;
                }).collect(Collectors.toList())))
        .build();
  }

  private List<Style> layers(Config style) {
    List<Style> styles = style.getStylesheets().stream()
        .flatMap(sheet -> sheet.getStyles().stream())
        .collect(Collectors.toList());
    return Lists.reverse(styles);
  }

}
