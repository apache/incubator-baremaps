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

package com.baremaps.importer.database;

import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.ChangeHandler;
import com.baremaps.util.tile.Tile;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.locationtech.jts.geom.Geometry;

public class DeltaHandler implements ChangeHandler {

  private final ProjectionTransformer projectionTransformer;

  private final NodeTable nodeTable;

  private final WayTable wayTable;

  private final RelationTable relationTable;

  private final int zoom;

  private final Set<Tile> tiles = new HashSet<>();

  @Inject
  public DeltaHandler(
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      ProjectionTransformer projectionTransformer,
      int zoom) {
    this.projectionTransformer = projectionTransformer;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
    this.zoom = zoom;
  }

  @Override
  public void onNodeCreate(Node node) throws Exception {
    // do nothing
  }

  @Override
  public void onNodeModify(Node node) throws Exception {
    nodeTable.select(node.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onNodeDelete(Node node) throws Exception {
    nodeTable.select(node.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onWayCreate(Way way) throws Exception {
    // do nothing
  }


  @Override
  public void onWayModify(Way way) throws Exception {
    wayTable.select(way.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onWayDelete(Way way) throws Exception {
    wayTable.select(way.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onRelationCreate(Relation relation) throws Exception {
    // do nothing
  }

  @Override
  public void onRelationModify(Relation relation) throws Exception {
    relationTable.select(relation.getId())
        .getGeometry()
        .ifPresent(this::handleGeometry);
  }

  @Override
  public void onRelationDelete(Relation relation) throws Exception {
    relationTable.select(relation.getId())
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
