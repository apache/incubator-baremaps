package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;

import java.util.function.Function;

public record DataBlockMapper(
  Function<Node, Node> nodeMapper,
  Function<Way, Way> wayMapper,
  Function<Relation, Relation> relationMapper
) implements Function<DataBlock, DataBlock> {

  @Override
  public DataBlock apply(DataBlock dataBlock) {
    var denseNodes = dataBlock.denseNodes().stream().map(nodeMapper).toList();
    var nodes = dataBlock.nodes().stream().map(nodeMapper).toList();
    var ways = dataBlock.ways().stream().map(wayMapper).toList();
    var relations = dataBlock.relations().stream().map(relationMapper).toList();
    return new DataBlock(denseNodes, nodes, ways, relations);
  }

}
