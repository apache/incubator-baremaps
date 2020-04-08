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
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.osmxml.Change;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TileChangeConsumer implements Consumer<Change> {

  private final NodeBuilder nodeStore;
  private final WayBuilder wayStore;
  private final RelationBuilder relationStore;

  private final Set<Tile> tiles = new HashSet<>();

  public TileChangeConsumer(NodeBuilder nodeStore, WayBuilder wayStore,
      RelationBuilder relationStore) {
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(Change change) {
    Entity entity = change.getEntity();
    if (entity instanceof Node) {
      Node node = (Node) entity;
      tiles.addAll(Tile.getTiles(nodeStore.build(node), 14)
          .collect(Collectors.toList()));
    } else if (entity instanceof Way) {
      Way way = (Way) entity;
      tiles.addAll(Tile.getTiles(wayStore.build(way), 14)
          .collect(Collectors.toList()));
    } else if (entity instanceof Relation) {
      Relation relation = (Relation) entity;
      tiles.addAll(Tile.getTiles(relationStore.build(relation), 14)
          .collect(Collectors.toList()));
    }
  }

  public Set<Tile> getTiles() {
    return tiles;
  }

}
