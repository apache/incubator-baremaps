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
package com.baremaps.osm.parser;

import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.List;
import javax.inject.Inject;

class PBFFileBlockGeometryHandler implements PBFFileBlockHandler {

  private final NodeBuilder nodeBuilder;

  private final WayBuilder wayBuilder;

  private final RelationBuilder relationBuilder;

  private final PBFFileBlockHandler handler;

  @Inject
  public PBFFileBlockGeometryHandler(
      NodeBuilder nodeBuilder,
      WayBuilder wayBuilder,
      RelationBuilder relationBuilder,
      PBFFileBlockHandler handler) {
    this.nodeBuilder = nodeBuilder;
    this.wayBuilder = wayBuilder;
    this.relationBuilder = relationBuilder;
    this.handler = handler;
  }

  @Override
  public void onHeader(Header header) throws Exception {
    handler.onHeader(header);
  }

  @Override
  public void onNodes(List<Node> nodes) throws Exception {
    nodes.forEach(node -> node.setGeometry(nodeBuilder.build(node)));
    handler.onNodes(nodes);
  }

  @Override
  public void onWays(List<Way> ways) throws Exception {
    ways.forEach(way -> way.setGeometry(wayBuilder.build(way)));
    handler.onWays(ways);
  }

  @Override
  public void onRelations(List<Relation> relations) throws Exception {
    relations.forEach(relation -> relation.setGeometry(relationBuilder.build(relation)));
    handler.onRelations(relations);
  }

  @Override
  public void onComplete() throws Exception {
    handler.onComplete();
  }

  @Override
  public void onError(Throwable error) throws Exception {
    handler.onError(error);
  }
}
