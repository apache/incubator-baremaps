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

import com.baremaps.osm.geometry.NodeGeometryBuilder;
import com.baremaps.osm.geometry.RelationGeometryBuilder;
import com.baremaps.osm.geometry.WayGeometryBuilder;
import com.baremaps.osm.model.Change;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.function.Consumer;

public class StoreUpdater implements Consumer<Change> {

  private final WayGeometryBuilder wayGeometryBuilder;
  private final RelationGeometryBuilder relationGeometryBuilder;
  private final NodeGeometryBuilder nodeGeometryBuilder;

  private final PostgisNodeStore nodeStore;
  private final PostgisWayStore wayStore;
  private final PostgisRelationStore relationStore;

  public StoreUpdater(
      NodeGeometryBuilder nodeGeometryBuilder, WayGeometryBuilder wayGeometryBuilder,
      RelationGeometryBuilder relationGeometryBuilder, PostgisNodeStore nodeStore,
      PostgisWayStore wayStore,
      PostgisRelationStore relationStore) {
    this.nodeGeometryBuilder = nodeGeometryBuilder;
    this.wayGeometryBuilder = wayGeometryBuilder;
    this.relationGeometryBuilder = relationGeometryBuilder;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(Change change) {
    try {
      Entity entity = change.getEntity();
      if (entity instanceof Node) {
        Node node = (Node) entity;
        switch (change.getType()) {
          case create:
          case modify:
            node.setGeometry(nodeGeometryBuilder.build(node));
            nodeStore.put(node);
            break;
          case delete:
            nodeStore.delete(node.getId());
            break;
          default:
            break;
        }
      } else if (entity instanceof Way) {
        Way way = (Way) entity;
        switch (change.getType()) {
          case create:
          case modify:
            way.setGeometry(wayGeometryBuilder.build(way));
            wayStore.put(way);
            break;
          case delete:
            wayStore.delete(way.getId());
            break;
          default:
            break;
        }
      } else if (entity instanceof Relation) {
        Relation relation = (Relation) entity;
        switch (change.getType()) {
          case create:
          case modify:
            relation.setGeometry(relationGeometryBuilder.build(relation));
            relationStore.put(relation);
            break;
          case delete:
            relationStore.delete(relation.getId());
            break;
          default:
            break;
        }
      }
    } catch (StoreException e) {
      throw new RuntimeException(e);
    }
  }

}
