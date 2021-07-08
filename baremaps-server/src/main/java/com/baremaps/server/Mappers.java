package com.baremaps.server;

import com.baremaps.config.tileset.Tileset;
import com.baremaps.tile.postgres.PostgresQuery;
import java.util.List;
import java.util.stream.Collectors;

public class Mappers {

  private Mappers() {

  }

  public static List<PostgresQuery> map(Tileset tileset) {
    return tileset.getLayers().stream()
        .flatMap(layer -> layer.getQueries().stream()
            .map(query -> new PostgresQuery(layer.getId(), query.getMinZoom(), query.getMaxZoom(), query.getSql())))
        .collect(Collectors.toList());
  }

}