/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.openstreetmap.repository;



import java.util.function.Consumer;
import org.apache.baremaps.openstreetmap.model.Change;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.stream.StreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A consumer for importing OpenStreetMap changes in a database. */
public class ChangeImporter implements Consumer<Change> {

  private static final Logger logger = LoggerFactory.getLogger(ChangeImporter.class);

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
  public ChangeImporter(
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository) {
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Change change) {
    var nodes = change.getEntities().stream()
        .filter(entity -> entity instanceof Node)
        .map(entity -> (Node) entity)
        .toList();
    var ways = change.getEntities().stream()
        .filter(entity -> entity instanceof Way)
        .map(entity -> (Way) entity)
        .toList();
    var relations = change.getEntities().stream()
        .filter(entity -> entity instanceof Relation)
        .map(entity -> (Relation) entity)
        .toList();
    try {
      switch (change.getType()) {
        case CREATE -> {
          if (!nodes.isEmpty()) {
            logger.info("Creating {} nodes", nodes.size());
            nodeRepository.copy(nodes);
          }
          if (!ways.isEmpty()) {
            logger.info("Creating {} ways", ways.size());
            wayRepository.copy(ways);
          }
          if (!relations.isEmpty()) {
            logger.info("Creating {} relations", relations.size());
            relationRepository.copy(relations);
          }
        }
        case MODIFY -> {
          if (!nodes.isEmpty()) {
            logger.info("Modifying {} nodes", nodes.size());
            nodeRepository.put(nodes);
          }
          if (!ways.isEmpty()) {
            logger.info("Modifying {} ways", ways.size());
            wayRepository.put(ways);
          }
          if (!relations.isEmpty()) {
            logger.info("Modifying {} relations", relations.size());
            relationRepository.put(relations);
          }
        }
        case DELETE -> {
          if (!nodes.isEmpty()) {
            logger.info("Deleting {} nodes", nodes.size());
            nodeRepository.delete(nodes.stream().map(Node::id).toList());
          }
          if (!ways.isEmpty()) {
            logger.info("Deleting {} ways", ways.size());
            wayRepository.delete(ways.stream().map(Way::id).toList());
          }
          if (!relations.isEmpty()) {
            logger.info("Deleting {} relations", relations.size());
            relationRepository.delete(relations.stream().map(Relation::id).toList());
          }
        }
      }
    } catch (RepositoryException e) {
      throw new StreamException(e);
    }
  }


}
