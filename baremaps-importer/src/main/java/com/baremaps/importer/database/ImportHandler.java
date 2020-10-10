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

import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.pbf.FileBlockHandler;
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
  public void onHeader(Header header) throws DatabaseException {
    headerTable.insert(header);
  }

  @Override
  public void onNodes(List<Node> nodes) throws DatabaseException {
    nodeTable.copy(nodes);
  }

  @Override
  public void onWays(List<Way> ways) throws DatabaseException {
    wayTable.copy(ways);
  }

  @Override
  public void onRelations(List<Relation> relations) throws DatabaseException {
    relationTable.copy(relations);
  }

}
