package org.apache.baremaps.vectortile.tilejsonextended;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.vectortile.tilejson.VectorLayer;
import org.apache.baremaps.vectortile.tileset.TilesetQuery;

public class VectorLayerExtended extends VectorLayer {

  @JsonProperty("queries")
  List<TilesetQuery> queries = new ArrayList<>();

  public List<TilesetQuery> getQueries() {
    return queries;
  }
}
