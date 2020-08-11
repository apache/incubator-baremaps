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

package com.baremaps.osm.store;

import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.parser.XMLChangeHandler;
import javax.inject.Inject;

public class StoreUpdateHandler implements XMLChangeHandler {

  private final WayBuilder wayBuilder;

  private final RelationBuilder relationBuilder;

  private final NodeBuilder nodeBuilder;

  private final PostgisNodeStore nodeStore;

  private final PostgisWayStore wayStore;

  private final PostgisRelationStore relationStore;

  @Inject
  public StoreUpdateHandler(
      NodeBuilder nodeBuilder,
      WayBuilder wayBuilder,
      RelationBuilder relationBuilder,
      PostgisNodeStore nodeStore,
      PostgisWayStore wayStore,
      PostgisRelationStore relationStore) {
    this.nodeBuilder = nodeBuilder;
    this.wayBuilder = wayBuilder;
    this.relationBuilder = relationBuilder;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void onNodeCreate(Node node) {
    // do nothing
  }

  @Override
  public void onNodeModify(Node node) throws StoreException {
    node.setGeometry(nodeBuilder.build(node));
    nodeStore.put(node);
  }

  @Override
  public void onNodeDelete(Node node) throws StoreException {
    nodeStore.delete(node.getId());
  }

  @Override
  public void onWayCreate(Way way) {
    // do nothing
  }

  @Override
  public void onWayModify(Way way) throws StoreException {
    way.setGeometry(wayBuilder.build(way));
    wayStore.put(way);
  }

  @Override
  public void onWayDelete(Way way) throws StoreException {
    wayStore.delete(way.getId());
  }

  @Override
  public void onRelationCreate(Relation relation) {
    // do nothing
  }

  @Override
  public void onRelationModify(Relation relation) throws StoreException {
    relation.setGeometry(relationBuilder.build(relation));
    relationStore.put(relation);
  }

  @Override
  public void onRelationDelete(Relation relation) throws StoreException {
    relationStore.delete(relation.getId());
  }

}
