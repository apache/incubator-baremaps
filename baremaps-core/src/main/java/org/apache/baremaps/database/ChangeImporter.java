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
import org.apache.baremaps.database.repository.RepositoryException;
import org.apache.baremaps.openstreetmap.model.Change;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.apache.baremaps.stream.StreamException;

/** A consumer for importing OpenStreetMap changes in a database. */
public class ChangeImporter implements Consumer<Change> {

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
  public ChangeImporter(Repository<Long, Node> nodeRepository, Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository) {
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Change change) {
    try {
      for (var entity : change.getEntities()) {
        switch (change.getType()) {
          case CREATE:
          case MODIFY:
            if (entity instanceof Node node) {
              nodeRepository.put(node);
            } else if (entity instanceof Way way) {
              wayRepository.put(way);
            } else if (entity instanceof Relation relation) {
              relationRepository.put(relation);
            }
            break;
          case DELETE:
            if (entity instanceof Node node) {
              nodeRepository.delete(node.getId());
            } else if (entity instanceof Way way) {
              wayRepository.delete(way.getId());
            } else if (entity instanceof Relation relation) {
              relationRepository.delete(relation.getId());
            }
            break;
        }
      }
    } catch (RepositoryException e) {
      throw new StreamException(e);
    }
  }


}
