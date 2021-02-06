package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockHandler;
import java.util.List;

public class DataBlock extends Block {

  private final List<Node> denseNodes;
  private final List<Node> nodes;
  private final List<Way> ways;
  private final List<Relation> relations;

  public DataBlock(Blob blob, List<Node> denseNodes, List<Node> nodes, List<Way> ways, List<Relation> relations) {
    super(blob);
    this.denseNodes = denseNodes;
    this.nodes = nodes;
    this.ways = ways;
    this.relations = relations;
  }

  public List<Node> getDenseNodes() {
    return denseNodes;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public List<Way> getWays() {
    return ways;
  }

  public List<Relation> getRelations() {
    return relations;
  }

  @Override
  public void handle(BlockHandler handler) throws Exception {
    handler.handle(this);
  }

}
