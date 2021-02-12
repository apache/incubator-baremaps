package com.baremaps.server;

import com.baremaps.config.Config;
import com.baremaps.config.Layer;
import com.baremaps.config.Style;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlueprintMapper implements Function<Config, Object> {

  @Override
  public Object apply(Config source) {
    return ImmutableSortedMap.naturalOrder()
        .put("id", "baremaps")
        .put("version", 8)
        .put("sprite", "")
        .put("glyphs", "https://daglvzoa8byzd.cloudfront.net/{fontstack}/{range}.pbf")
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
                .build())
            .build())
        .put("layers", styles(source).stream().map(style -> {
          Map<String, Object> map = new TreeMap<>();
          map.put("id", style.getId());
          map.put("source", Optional.ofNullable(style.getSource()).orElse("baremaps"));
          map.put("source-layer", style.getSourceLayer());
          map.put("type", style.getType());
          map.put("minzoom", style.getMinZoom());
          map.put("maxzoom", style.getMaxZoom());
          map.put("layout", style.getLayout());
          map.put("metadata", style.getMetadata());
          map.put("filter", style.getFilter());
          map.put("paint", style.getPaint());
          return map;
        }).collect(Collectors.toList()))
        .build();
  }

  private List<Style> styles(Config source) {
    List<Style> styles = new ArrayList<>();
    styles.add(background());
    for (Layer layer : source.getLayers()) {
      styles.addAll(style(layer));
    }
    return styles;
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

  private List<Style> style(Layer layer) {
    switch (layer.getType()) {
      case "point":
        return Arrays.asList(point(layer, String.format("%s_point", layer.getId())));
      case "line":
        return Arrays.asList(linestring(layer, String.format("%s_linestring", layer.getId())));
      case "polygon":
        return Arrays.asList(polygon(layer, String.format("%s_polygon", layer.getId())));
      case "geometry":
        return Arrays.asList(
            point(layer, String.format("%s_point", layer.getId())),
            linestring(layer, String.format("%s_linestring", layer.getId())));
      default:
        return Arrays.asList();
    }
  }

  private Style point(Layer layer, String id) {
    Style style = new Style();
    style.setId(id);
    style.setSourceLayer(layer.getId());
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

  private Style linestring(Layer layer, String id) {
    Style style = new Style();
    style.setId(id);
    style.setSourceLayer(layer.getId());
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

  private Style polygon(Layer layer, String id) {
    Style style = new Style();
    style.setId(id);
    style.setSourceLayer(layer.getId());
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
