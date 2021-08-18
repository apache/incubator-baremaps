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

package com.baremaps.osm.database;

import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ChangeConsumer;
import com.baremaps.osm.handler.EntityConsumerAdapter;

public class SaveChangeConsumer implements ChangeConsumer {

  private final EntityTable<Node> nodeTable;
  private final EntityTable<Way> wayTable;
  private final EntityTable<Relation> relationTable;

  public SaveChangeConsumer(
      EntityTable<Node> nodeTable, EntityTable<Way> wayTable, EntityTable<Relation> relationTable) {
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void match(Change change) throws Exception {
    for (Entity entity : change.getEntities()) {
      entity.visit(
          new EntityConsumerAdapter() {
            @Override
            public void match(Node node) throws Exception {
              switch (change.getType()) {
                case CREATE:
                case MODIFY:
                  nodeTable.insert(node);
                  break;
                case DELETE:
                  nodeTable.delete(node.getId());
                  break;
              }
            }

            @Override
            public void match(Way way) throws Exception {
              switch (change.getType()) {
                case CREATE:
                case MODIFY:
                  wayTable.insert(way);
                  break;
                case DELETE:
                  wayTable.delete(way.getId());
                  break;
              }
            }

            @Override
            public void match(Relation relation) throws Exception {
              switch (change.getType()) {
                case CREATE:
                case MODIFY:
                  relationTable.insert(relation);
                  break;
                case DELETE:
                  relationTable.delete(relation.getId());
                  break;
              }
            }
          });
    }
  }
}
