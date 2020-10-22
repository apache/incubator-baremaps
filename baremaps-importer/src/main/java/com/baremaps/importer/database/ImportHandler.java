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

import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.pbf.DataBlock;
import com.baremaps.osm.reader.pbf.FileBlockHandler;
import com.baremaps.osm.reader.pbf.HeaderBlock;
import java.util.List;
import javax.inject.Inject;

public class ImportHandler implements FileBlockHandler {

  private final HeaderTable headerTable;

  private final Table<Node> nodeTable;

  private final Table<Way> wayTable;

  private final Table<Relation> relationTable;

  @Inject
  public ImportHandler(
      HeaderTable headerTable,
      Table<Node> nodeTable,
      Table<Way> wayTable,
      Table<Relation> relationTable) {
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void onHeaderBlock(HeaderBlock headerBlock) throws DatabaseException {
    headerTable.insert(headerBlock.getHeader());
  }

  @Override
  public void onDataBlock(DataBlock dataBlock) throws DatabaseException {
    List<Node> denseNodes = dataBlock.getDenseNodes();
    if (denseNodes.size() > 0) {
      nodeTable.copy(denseNodes);
    }
    List<Node> nodes = dataBlock.getNodes();
    if (nodes.size() > 0) {
      nodeTable.copy(nodes);
    }
    List<Way> ways = dataBlock.getWays();
    if (ways.size() > 0) {
      wayTable.copy(ways);
    }
    List<Relation> relations = dataBlock.getRelations();
    if (relations.size() > 0) {
      relationTable.copy(relations);
    }
  }

}
