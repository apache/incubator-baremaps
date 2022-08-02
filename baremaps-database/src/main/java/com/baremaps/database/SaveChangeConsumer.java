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

package com.baremaps.database;

import com.baremaps.database.repository.Repository;
import com.baremaps.osm.function.ChangeConsumer;
import com.baremaps.osm.function.EntityConsumerAdapter;
import com.baremaps.osm.model.Change;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;

/** A consumer for saving OpenStreetMap changes in a database. */
public class SaveChangeConsumer implements ChangeConsumer {

  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;

  /**
   * Constructs a {@code SaveChangeConsumer}.
   *
   * @param nodeRepository the node table
   * @param wayRepository the way table
   * @param relationRepository the relation table
   */
  public SaveChangeConsumer(
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository) {
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
  }

  /** {@inheritDoc} */
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
                  nodeRepository.put(node);
                  break;
                case DELETE:
                  nodeRepository.delete(node.getId());
                  break;
              }
            }

            @Override
            public void match(Way way) throws Exception {
              switch (change.getType()) {
                case CREATE:
                case MODIFY:
                  wayRepository.put(way);
                  break;
                case DELETE:
                  wayRepository.delete(way.getId());
                  break;
              }
            }

            @Override
            public void match(Relation relation) throws Exception {
              switch (change.getType()) {
                case CREATE:
                case MODIFY:
                  relationRepository.put(relation);
                  break;
                case DELETE:
                  relationRepository.delete(relation.getId());
                  break;
              }
            }
          });
    }
  }
}
