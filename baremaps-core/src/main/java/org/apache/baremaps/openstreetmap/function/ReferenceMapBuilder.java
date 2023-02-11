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

package org.apache.baremaps.openstreetmap.function;



import java.util.List;
import java.util.function.Consumer;
import org.apache.baremaps.collection.DataMap;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Way;

/** A consumer that stores openstreetmap references in a map. */
public class ReferenceMapBuilder implements Consumer<Entity> {

  private final DataMap<List<Long>> referenceMap;

  /**
   * Constructs a {@code CacheBlockConsumer} with the provided map.
   *
   * @param referenceMap the map of references
   */
  public ReferenceMapBuilder(DataMap<List<Long>> referenceMap) {
    this.referenceMap = referenceMap;
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Entity entity) {
    if (entity instanceof Way way) {
      referenceMap.put(way.id(), way.getNodes());
    }
  }
}
