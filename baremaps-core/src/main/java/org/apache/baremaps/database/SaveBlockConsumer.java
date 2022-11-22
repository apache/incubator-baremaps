/*
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

package org.apache.baremaps.database;



import java.util.function.Consumer;
import org.apache.baremaps.database.repository.Repository;
import org.apache.baremaps.openstreetmap.model.*;
import org.apache.baremaps.stream.StreamException;

/** A consumer for saving OpenStreetMap blocks in a database. */
public class SaveBlockConsumer implements Consumer<Block> {

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
  public SaveBlockConsumer(Repository<Long, Header> headerRepository,
      Repository<Long, Node> nodeRepository, Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository) {
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
  }

  @Override
  public void accept(Block block) {
    try {
      if (block instanceof HeaderBlock headerBlock) {
        headerRepository.put(headerBlock.header());
      } else if (block instanceof DataBlock dataBlock) {
        nodeRepository.copy(dataBlock.denseNodes());
        nodeRepository.copy(dataBlock.nodes());
        wayRepository.copy(dataBlock.ways());
        relationRepository.copy(dataBlock.relations());
      }
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }
}
