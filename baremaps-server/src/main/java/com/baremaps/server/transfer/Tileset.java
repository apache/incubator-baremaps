package com.baremaps.server.transfer;

import com.baremaps.config.Config;
import com.baremaps.config.Query;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Tileset {

  public static Object toTileset(Config config) {
    List<Query> queries = config.getLayers().stream()
        .flatMap(layer -> layer.getQueries().stream())
        .collect(Collectors.toList());
    double minZoom = queries.stream().mapToDouble(query -> query.getMinZoom()).min().orElse(0);
    double maxZoom = queries.stream().mapToDouble(query -> query.getMaxZoom()).max().orElse(24);
    return ImmutableSortedMap.naturalOrder()
        .put("tilejson", "2.2.0")
        .put("bounds", config.getBounds().asList())
        .put("center", config.getCenter().asList())
        .put("format", "pbf")
        .put("minzoom", minZoom)
        .put("maxzoom", maxZoom)
        .put("vector_layers", config.getLayers().stream().map(layer -> {
          List<Query> layerQueries = layer.getQueries();
          double layerMinZoom = layerQueries.stream().mapToDouble(query -> query.getMinZoom()).min().orElse(0);
          double layerMaxZoom = layerQueries.stream().mapToDouble(query -> query.getMaxZoom()).max().orElse(24);
          Map<String, Object> map = new TreeMap<>();
          map.put("id", layer.getId());
          map.put("description", layer.getDescription());
          map.put("minzoom", layerMinZoom);
          map.put("maxzoom", layerMaxZoom);
          return map;
        }).collect(Collectors.toList()))
        .build();
  }

}
