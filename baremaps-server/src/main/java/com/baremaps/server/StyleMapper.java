package com.baremaps.server;

import com.baremaps.config.Config;
import com.baremaps.config.Style;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StyleMapper implements Function<Config, Object> {

  @Override
  public Object apply(Config source) {
    return ImmutableSortedMap.naturalOrder()
        .put("id", source.getName())
        .put("version", source.getVersion())
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
                .map(layer -> ImmutableSortedMap.naturalOrder()
                    .put("id", layer.getId())
                    .put("source", Optional.ofNullable(layer.getSource()).orElse("baremaps"))
                    .put("source-layer", layer.getSourceLayer())
                    .put("type", layer.getType())
                    .put("minzoom", layer.getMinZoom())
                    .put("maxzoom", layer.getMaxZoom())
                    .put("layout", layer.getLayout())
                    .put("metadata", layer.getMetadata())
                    .put("filter", layer.getFilter())
                    .put("paint", layer.getPaint())
                ).collect(Collectors.toList())))
        .build();
  }

  private List<Style> layers(Config style) {
    List<Style> styles = style.getStylesheets().stream()
        .flatMap(sheet -> sheet.getStyles().stream())
        .collect(Collectors.toList());
    return Lists.reverse(styles);
  }

}
