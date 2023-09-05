package org.apache.baremaps.vectortile.tilejsonextended;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;
import org.apache.baremaps.vectortile.tilejson.TileJSON;
import org.apache.baremaps.vectortile.tilejson.VectorLayer;


/**
 * Implementation of TileJSON with custom additional fields for baremaps.
 */
public class TileJSONExtended extends TileJSON {

  @JsonProperty("vector_layers")
  List<VectorLayerExtended> vectorLayersExtended;

  @JsonGetter("vector_layers")
  public List<VectorLayerExtended> getVectorLayersExtended() {
    return this.vectorLayersExtended;
  }

  @JsonSetter("vector_layers")
  public void setVectorLayersExtended(List<VectorLayerExtended> vectorLayersExtended) {
    this.vectorLayersExtended = vectorLayersExtended;
    super.setVectorLayers(vectorLayersExtended);
  }

}
