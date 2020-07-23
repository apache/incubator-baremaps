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
import com.baremaps.osm.pbf.FileBlock;
import com.baremaps.osm.pbf.HeaderBlock;
import com.baremaps.osm.pbf.PrimitiveBlock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StoreImporter implements Consumer<FileBlock> {

  private final PostgisHeaderStore headerTable;

  private final NodeGeometryBuilder nodeGeometryBuilder;

  private final WayGeometryBuilder wayGeometryBuilder;

  private final RelationGeometryBuilder relationGeometryBuilder;

  private final Store<NodeEntity> nodeStore;

  private final Store<WayEntity> wayStore;

  private final Store<RelationEntity> relationStore;

  public StoreImporter(
      PostgisHeaderStore headerTable,
      NodeGeometryBuilder nodeGeometryBuilder,
      WayGeometryBuilder wayGeometryBuilder,
      RelationGeometryBuilder relationGeometryBuilder,
      Store<NodeEntity> nodeStore,
      Store<WayEntity> wayStore,
      Store<RelationEntity> relationStore) {
    this.headerTable = headerTable;
    this.nodeGeometryBuilder = nodeGeometryBuilder;
    this.nodeStore = nodeStore;
    this.wayGeometryBuilder = wayGeometryBuilder;
    this.wayStore = wayStore;
    this.relationGeometryBuilder = relationGeometryBuilder;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(FileBlock block) {
    switch (block.getType()) {
      case OSMHeader:
        accept(block.toHeaderBlock());
        break;
      case OSMData:
        accept(block.toPrimitiveBlock());
        break;
      default:
        break;
    }
  }

  private void accept(HeaderBlock headerBlock) {
    try {
      headerTable.insert(headerBlock);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void accept(PrimitiveBlock primitiveBlock) {
    try {
      nodeStore.copy(
          primitiveBlock.getDenseNodes().stream()
              .map(node -> new NodeEntity(
                  node.getId(),
                  node.getVersion(),
                  node.getTimestamp(),
                  node.getChangeset(),
                  node.getUserId(),
                  node.getTags(),
                  nodeGeometryBuilder.build(node)))
              .collect(Collectors.toList()));
      nodeStore.copy(
          primitiveBlock.getNodes().stream()
              .map(node -> new NodeEntity(
                  node.getId(),
                  node.getVersion(),
                  node.getTimestamp(),
                  node.getChangeset(),
                  node.getUserId(),
                  node.getTags(),
                  nodeGeometryBuilder.build(node)))
              .collect(Collectors.toList()));
      wayStore.copy(
          primitiveBlock.getWays().stream()
              .map(way -> new WayEntity(
                  way.getId(),
                  way.getVersion(),
                  way.getTimestamp(),
                  way.getChangeset(),
                  way.getUserId(),
                  way.getTags(),
                  way.getNodes(),
                  wayGeometryBuilder.build(way)))
              .collect(Collectors.toList()));
      relationStore.copy(
          primitiveBlock.getRelations().stream()
              .map(relation -> new RelationEntity(
                  relation.getId(),
                  relation.getVersion(),
                  relation.getTimestamp(),
                  relation.getChangeset(),
                  relation.getUserId(),
                  relation.getTags(),
                  relation.getMembers().stream().map(m -> m.getRef()).toArray(Long[]::new),
                  relation.getMembers().stream().map(m -> m.getType().name()).toArray(String[]::new),
                  relation.getMembers().stream().map(m -> m.getRole()).toArray(String[]::new),
                  relationGeometryBuilder.build(relation)))
              .collect(Collectors.toList()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
