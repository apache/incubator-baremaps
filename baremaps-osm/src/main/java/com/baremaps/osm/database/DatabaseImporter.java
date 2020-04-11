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

import com.baremaps.osm.osmpbf.FileBlockConsumer;
import com.baremaps.osm.osmpbf.HeaderBlock;
import com.baremaps.osm.osmpbf.PrimitiveBlock;
import com.baremaps.osm.store.Store.Entry;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
import java.util.stream.Collectors;

public class DatabaseImporter extends FileBlockConsumer {

  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  public DatabaseImporter(
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable) {
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {
    try {
      headerTable.insert(headerBlock);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try {
      // TODO: Build node geometries
      nodeTable.importAll(
          primitiveBlock.getDenseNodes().stream()
              .map(node -> new NodeTable.Node(node.getInfo().getId(), node.getInfo().getVersion(),
                  node.getInfo().getTimestamp(), node.getInfo().getChangeset(), node.getInfo().getUserId(),
                  node.getInfo().getTags(), null))
              .collect(Collectors.toList()));
      wayTable.importAll(
          primitiveBlock.getWays().stream()
              .map(way -> new WayTable.Way(way.getInfo().getId(), way.getInfo().getVersion(),
                  way.getInfo().getTimestamp(), way.getInfo().getChangeset(), way.getInfo().getUserId(),
                  way.getInfo().getTags(), way.getNodes(), null))
              .collect(Collectors.toList()));
      relationTable.importAll(
          primitiveBlock.getRelations().stream()
              .map(relation -> new Entry<>(relation.getInfo().getId(), relation))
              .collect(Collectors.toList()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
