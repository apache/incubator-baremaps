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
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.parser.XMLChangeHandler;
import com.baremaps.tiles.Tile;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.locationtech.jts.geom.Geometry;

public class StoreDeltaHandler implements XMLChangeHandler {

  private final ProjectionTransformer projectionTransformer;

  private final WayBuilder wayBuilder;

  private final RelationBuilder relationGeometryBuilder;

  private final NodeBuilder nodeBuilder;

  private final PostgisNodeStore nodeStore;

  private final PostgisWayStore wayStore;

  private final PostgisRelationStore relationStore;

  private final int zoom;

  private final Set<Tile> tiles = new HashSet<>();

  @Inject
  public StoreDeltaHandler(
      NodeBuilder nodeBuilder,
      WayBuilder wayBuilder,
      RelationBuilder relationBuilder,
      PostgisNodeStore nodeStore,
      PostgisWayStore wayStore,
      PostgisRelationStore relationStore,
      ProjectionTransformer projectionTransformer,
      int zoom) {
    this.projectionTransformer = projectionTransformer;
    this.nodeBuilder = nodeBuilder;
    this.wayBuilder = wayBuilder;
    this.relationGeometryBuilder = relationBuilder;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
    this.zoom = zoom;
  }

  @Override
  public void onNodeModify(Node node) throws Exception {
    nodeStore.get(node.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onNodeDelete(Node node) throws Exception {
    nodeStore.get(node.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }


  @Override
  public void onWayModify(Way way) throws Exception {
    nodeStore.get(way.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onWayDelete(Way way) throws Exception {
    nodeStore.get(way.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onRelationModify(Relation relation) throws Exception {
    nodeStore.get(relation.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onRelationDelete(Relation relation) throws Exception {
    nodeStore.get(relation.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  private void handleGeometry(Geometry geometry) {
    tiles.addAll(
        Tile.getTiles(projectionTransformer.transform(geometry).getEnvelopeInternal(), zoom)
            .collect(Collectors.toList()));
  }

  public Set<Tile> getTiles() {
    return tiles;
  }

}
