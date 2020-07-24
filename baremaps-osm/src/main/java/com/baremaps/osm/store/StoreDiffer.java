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
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.geometry.RelationGeometryBuilder;
import com.baremaps.osm.geometry.WayGeometryBuilder;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.model.Change;
import com.baremaps.tiles.Tile;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;

public class StoreDiffer implements Consumer<Change> {

  private final ProjectionTransformer projectionTransformer;

  private final WayGeometryBuilder wayGeometryBuilder;
  private final RelationGeometryBuilder relationGeometryBuilder;
  private final NodeGeometryBuilder nodeGeometryBuilder;

  private final PostgisNodeStore nodeStore;
  private final PostgisWayStore wayStore;
  private final PostgisRelationStore relationStore;

  private final int zoom;

  private final Set<Tile> tiles = new HashSet<>();

  public StoreDiffer(
      NodeGeometryBuilder nodeGeometryBuilder, WayGeometryBuilder wayGeometryBuilder, RelationGeometryBuilder relationGeometryBuilder,
      PostgisNodeStore nodeStore, PostgisWayStore wayStore, PostgisRelationStore relationStore,
      ProjectionTransformer projectionTransformer,
      int zoom) {
    this.projectionTransformer = projectionTransformer;
    this.nodeGeometryBuilder = nodeGeometryBuilder;
    this.wayGeometryBuilder = wayGeometryBuilder;
    this.relationGeometryBuilder = relationGeometryBuilder;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
    this.zoom = zoom;
  }

  @Override
  public void accept(Change change) {
    Geometry geometry = geometry(change);
    if (geometry != null) {
      tiles.addAll(
          Tile.getTiles(projectionTransformer.transform(geometry).getEnvelopeInternal(), zoom)
              .collect(Collectors.toList()));
    }
  }

  private final Geometry geometry(Change change) {
    Entity entity = change.getEntity();
    switch (change.getType()) {
      case delete:
      case modify:
        if (entity instanceof Node) {
          return nodeStore.get(entity.getId()).getGeometry().get();
        } else if (entity instanceof Way) {
          return wayStore.get(entity.getId()).getGeometry().get();
        } else if (entity instanceof Relation) {
          return relationStore.get(entity.getId()).getGeometry().get();
        } else {
          return null;
        }
      case create:
        if (entity instanceof Node) {
          return nodeGeometryBuilder.build((Node) entity);
        } else if (entity instanceof Way) {
          return wayGeometryBuilder.build((Way) entity);
        } else if (entity instanceof Relation) {
          return relationGeometryBuilder.build((Relation) entity);
        } else {
          return null;
        }
      default:
        return null;
    }
  }

  public Set<Tile> getTiles() {
    return tiles;
  }

}
