/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.osm.database;

import com.baremaps.osm.osmxml.Change;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.function.Consumer;

public class DatabaseUpdater implements Consumer<Change> {

  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  public DatabaseUpdater(
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable) {
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void accept(Change change) {
    Entity entity = change.getEntity();
    if (entity instanceof Node) {
      Node node = (Node) entity;
      switch (change.getType()) {
        case create:
        case modify:
          // TODO: Build node geometry
          nodeTable.insert(new NodeTable.Node(node.getInfo().getId(), node.getInfo().getVersion(),
              node.getInfo().getTimestamp(), node.getInfo().getChangeset(), node.getInfo().getUserId(),
              node.getInfo().getTags(), null));
          break;
        case delete:
          nodeTable.delete(node.getInfo().getId());
          break;
        default:
          break;
      }
    } else if (entity instanceof Way) {
      Way way = (Way) entity;
      switch (change.getType()) {
        case create:
        case modify:
          wayTable.insert(new WayTable.Way(way.getInfo().getId(), way.getInfo().getVersion(),
              way.getInfo().getTimestamp(), way.getInfo().getChangeset(), way.getInfo().getUserId(),
              way.getInfo().getTags(), way.getNodes(), null));
          break;
        case delete:
          wayTable.delete(way.getInfo().getId());
          break;
        default:
          break;
      }
    } else if (entity instanceof Relation) {
      Relation relation = (Relation) entity;
      switch (change.getType()) {
        case create:
        case modify:
          relationTable.insert(new RelationTable.Relation(
              relation.getInfo().getId(),
              relation.getInfo().getVersion(),
              relation.getInfo().getTimestamp(),
              relation.getInfo().getChangeset(),
              relation.getInfo().getUserId(),
              relation.getInfo().getTags(),
              relation.getMembers().stream().map(m -> m.getRef()).toArray(Long[]::new),
              relation.getMembers().stream().map(m -> m.getType()).toArray(String[]::new),
              relation.getMembers().stream().map(m -> m.getRole()).toArray(String[]::new),
              null));
          break;
        case delete:
          relationTable.delete(relation.getInfo().getId());
          break;
        default:
          break;
      }
    }
  }

}
