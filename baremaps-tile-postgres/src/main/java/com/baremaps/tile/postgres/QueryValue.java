package com.baremaps.tile.postgres;

import com.baremaps.config.tileset.Layer;
import com.baremaps.config.tileset.Query;
import net.sf.jsqlparser.statement.select.PlainSelect;

class QueryValue {

  private final Layer layer;
  private final Query query;
  private final PlainSelect value;

  protected QueryValue(Layer layer, Query query, PlainSelect parse) {
    this.layer = layer;
    this.query = query;
    this.value = parse;
  }

  public Layer getLayer() {
    return layer;
  }

  public Query getQuery() {
    return query;
  }

  public PlainSelect getValue() {
    return value;
  }

}
