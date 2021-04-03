package com.baremaps.server;

import com.baremaps.config.Config;
import com.baremaps.config.Layer;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Blueprint {

  public static Object toBlueprint(Config config) {
    return ImmutableSortedMap.naturalOrder()
        .put("id", "baremaps")
        .put("version", 8)
        .put("sprite", "")
        .put("glyphs", "https://tiles.baremaps.com/fonts/{fontstack}/{range}.pbf")
        .put("sources", ImmutableSortedMap.naturalOrder()
            .put("baremaps", ImmutableSortedMap.naturalOrder()
                .put("type", "vector")
                .put("url",String.format("http://%s:%s/tiles.json",
                    config.getServer().getHost(),
                    config.getServer().getPort()))
                .build())
            .build())
        .put("layers", styles(config).stream().map(style -> {
          Map<String, Object> map = new TreeMap<>();
          map.put("id", style.getId());
          map.put("source", Optional.ofNullable(style.getSource()).orElse("baremaps"));
          map.put("source-layer", style.getLayer());
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

  private static List<Style> styles(Config source) {
    List<Style> styles = new ArrayList<>();
    styles.add(background());
    for (Layer layer : source.getLayers()) {
      styles.addAll(style(layer));
    }
    return styles;
  }

  private static Style background() {
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

  private static List<Style> style(Layer layer) {
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

  private static Style point(Layer layer, String id) {
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

  private static Style linestring(Layer layer, String id) {
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

  private static Style polygon(Layer layer, String id) {
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


  public static class Style {

    private String id;

    private String type;

    private String metadata;

    private Integer minZoom;

    private Integer maxZoom;

    private String source;

    private String layer;

    private List<Object> filter;

    private Object layout;

    private Object paint;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getMetadata() {
      return metadata;
    }

    public void setMetadata(String metadata) {
      this.metadata = metadata;
    }

    public Integer getMinZoom() {
      return minZoom;
    }

    public void setMinZoom(int minZoom) {
      this.minZoom = minZoom;
    }

    public Integer getMaxZoom() {
      return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
      this.maxZoom = maxZoom;
    }

    public String getLayer() {
      return layer;
    }

    public void setLayer(String layer) {
      this.layer = layer;
    }

    public List<Object> getFilter() {
      return filter;
    }

    public void setFilter(List<Object> filter) {
      this.filter = filter;
    }

    public Object getLayout() {
      return layout;
    }

    public void setLayout(Object layout) {
      this.layout = layout;
    }

    public Object getPaint() {
      return paint;
    }

    public void setPaint(Object paint) {
      this.paint = paint;
    }

    public String getSource() {
      return source;
    }

    public void setSource(String source) {
      this.source = source;
    }
  }
}
