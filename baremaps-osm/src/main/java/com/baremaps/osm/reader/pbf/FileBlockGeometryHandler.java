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
package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.List;
import javax.inject.Inject;

public class FileBlockGeometryHandler implements FileBlockHandler {

  private final NodeBuilder nodeBuilder;

  private final WayBuilder wayBuilder;

  private final RelationBuilder relationBuilder;

  private final FileBlockHandler handler;

  @Inject
  public FileBlockGeometryHandler(
      NodeBuilder nodeBuilder,
      WayBuilder wayBuilder,
      RelationBuilder relationBuilder,
      FileBlockHandler handler) {
    this.nodeBuilder = nodeBuilder;
    this.wayBuilder = wayBuilder;
    this.relationBuilder = relationBuilder;
    this.handler = handler;
  }

  @Override
  public void onHeaderBlock(HeaderBlock headerBlock) throws Exception {
    handler.onHeaderBlock(headerBlock);
  }

  @Override
  public void onDataBlock(DataBlock dataBlock) throws Exception {
    List<Node> denseNodes = dataBlock.getDenseNodes();
    for (Node node : denseNodes) {
      node.setGeometry(nodeBuilder.build(node));
    }
    List<Node> nodes = dataBlock.getNodes();
    for (Node node: nodes) {
      node.setGeometry(nodeBuilder.build(node));
    }
    List<Way> ways = dataBlock.getWays();
    for (Way way : ways) {
      way.setGeometry(wayBuilder.build(way));
    }
    List<Relation> relations = dataBlock.getRelations();
    for (Relation relation : relations) {
      relation.setGeometry(relationBuilder.build(relation));
    }
    handler.onDataBlock(new DataBlock(denseNodes, nodes, ways, relations));
  }
}
