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
import com.baremaps.osm.function.BlockConsumerAdapter;
import com.baremaps.osm.model.DataBlock;
import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.HeaderBlock;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;

/** A consumer for saving OpenStreetMap blocks in a database. */
public class SaveBlockConsumer implements BlockConsumerAdapter {

  private final Repository<Long, Header> headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;

  /**
   * Constructs a {@code SaveBlockConsumer}.
   *
   * @param headerRepository the header table
   * @param nodeRepository the node table
   * @param wayRepository the way table
   * @param relationRepository the relation table
   */
  public SaveBlockConsumer(
      Repository<Long, Header> headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository) {
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
  }

  /** {@inheritDoc} */
  @Override
  public void match(HeaderBlock headerBlock) throws Exception {
    headerRepository.put(headerBlock.getHeader());
  }

  /** {@inheritDoc} */
  @Override
  public void match(DataBlock dataBlock) throws Exception {
    nodeRepository.copy(dataBlock.getDenseNodes());
    nodeRepository.copy(dataBlock.getNodes());
    wayRepository.copy(dataBlock.getWays());
    relationRepository.copy(dataBlock.getRelations());
  }
}
