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
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.model.Relation;
import org.apache.baremaps.openstreetmap.model.Way;
import org.locationtech.jts.geom.Coordinate;

/** A consumer that builds and sets the geometry of OpenStreetMap entities via side effects. */
public class EntityGeometryBuilder implements Consumer<Entity> {

  private final NodeGeometryBuilder nodeGeometryBuilder;
  private final WayGeometryBuilder wayGeometryBuilder;
  private final RelationGeometryBuilder relationGeometryBuilder;

  /**
   * Constructs a consumer that uses the provided caches to create and set geometries.
   *
   * @param coordinateMap the coordinate cache
   * @param referenceMap the reference cache
   */
  public EntityGeometryBuilder(LongDataMap<Coordinate> coordinateMap,
      LongDataMap<List<Long>> referenceMap) {
    this.nodeGeometryBuilder = new NodeGeometryBuilder();
    this.wayGeometryBuilder = new WayGeometryBuilder(coordinateMap);
    this.relationGeometryBuilder = new RelationGeometryBuilder(coordinateMap, referenceMap);
  }

  /** {@inheritDoc} */
  @Override
  public void accept(Entity entity) {
    if (entity instanceof Node node) {
      nodeGeometryBuilder.accept(node);
    } else if (entity instanceof Way way) {
      wayGeometryBuilder.accept(way);
    } else if (entity instanceof Relation relation) {
      relationGeometryBuilder.accept(relation);
    } else {
      // do nothing
    }
  }
}
