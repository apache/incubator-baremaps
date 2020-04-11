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

package com.baremaps.osm.store;

import com.baremaps.core.tile.Tile;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.osmxml.Change;
import com.baremaps.osm.osmxml.Change.Type;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TileChangeConsumer implements Consumer<Change> {

  private final NodeTable nodeStore;
  private final WayTable wayStore;
  private final RelationTable relationStore;

  private final Set<Tile> tiles = new HashSet<>();

  public TileChangeConsumer(
      NodeTable nodeStore, WayTable wayStore, RelationTable relationStore) {
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(Change change) {
    Entity entity = change.getEntity();
    if (change.getType().equals(Type.delete) || change.getType().equals(Type.modify)) {
      if (entity instanceof Node) {
        NodeTable.Node node = nodeStore.get(entity.getInfo().getId());
        System.out.println(node);
      } else if (entity instanceof Way) {
        WayTable.Way way = wayStore.get(entity.getInfo().getId());
        System.out.println(way);
      } else if (entity instanceof Relation) {
        RelationTable.Relation relation = relationStore.get(entity.getInfo().getId());
        System.out.println(relation);
      }
    }


  }

  public Set<Tile> getTiles() {
    return tiles;
  }

}
