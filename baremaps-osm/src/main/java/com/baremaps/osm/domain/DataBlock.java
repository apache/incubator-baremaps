/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockConsumer;
import com.baremaps.osm.handler.BlockFunction;
import java.util.List;

/** Represents a data block in an OpenStreetMap dataset. */
public class DataBlock extends Block {

  private final List<Node> denseNodes;
  private final List<Node> nodes;
  private final List<Way> ways;
  private final List<Relation> relations;

  public DataBlock(
      Blob blob,
      List<Node> denseNodes,
      List<Node> nodes,
      List<Way> ways,
      List<Relation> relations) {
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
  public void visit(BlockConsumer consumer) throws Exception {
    consumer.match(this);
  }

  @Override
  public <T> T visit(BlockFunction<T> function) throws Exception {
    return function.match(this);
  }
}
