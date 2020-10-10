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

import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.geometry.WayBuilder;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.reader.ChangeHandler;
import javax.inject.Inject;

public class UpdateHandler implements ChangeHandler {

  private final WayBuilder wayBuilder;

  private final RelationBuilder relationBuilder;

  private final NodeBuilder nodeBuilder;

  private final NodeTable nodeTable;

  private final WayTable wayTable;

  private final RelationTable relationTable;

  @Inject
  public UpdateHandler(
      NodeBuilder nodeBuilder,
      WayBuilder wayBuilder,
      RelationBuilder relationBuilder,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable) {
    this.nodeBuilder = nodeBuilder;
    this.wayBuilder = wayBuilder;
    this.relationBuilder = relationBuilder;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void onNodeCreate(Node node) {
    // do nothing
  }

  @Override
  public void onNodeModify(Node node) throws DatabaseException {
    node.setGeometry(nodeBuilder.build(node));
    nodeTable.insert(node);
  }

  @Override
  public void onNodeDelete(Node node) throws DatabaseException {
    nodeTable.delete(node.getId());
  }

  @Override
  public void onWayCreate(Way way) {
    // do nothing
  }

  @Override
  public void onWayModify(Way way) throws DatabaseException {
    way.setGeometry(wayBuilder.build(way));
    wayTable.insert(way);
  }

  @Override
  public void onWayDelete(Way way) throws DatabaseException {
    wayTable.delete(way.getId());
  }

  @Override
  public void onRelationCreate(Relation relation) {
    // do nothing
  }

  @Override
  public void onRelationModify(Relation relation) throws DatabaseException {
    relation.setGeometry(relationBuilder.build(relation));
    relationTable.insert(relation);
  }

  @Override
  public void onRelationDelete(Relation relation) throws DatabaseException {
    relationTable.delete(relation.getId());
  }

}
