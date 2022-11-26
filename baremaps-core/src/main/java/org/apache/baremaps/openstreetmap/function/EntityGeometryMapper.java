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
import java.util.function.Function;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.model.*;
import org.locationtech.jts.geom.Coordinate;

/** A function that adds geometry to an entity. */
public class EntityGeometryMapper<T extends Entity> implements Function<T, T> {

  private final NodeGeometryMapper nodeGeometryMapper;
  private final WayGeometryMapper wayGeometryMapper;
  private final RelationGeometryMapper relationGeometryMapper;

  /**
   * Constructs a function that adds geometry to an entity.
   *
   * @param coordinateMap The map of coordinates
   * @param referenceMap The map of references
   */
  public EntityGeometryMapper(LongDataMap<Coordinate> coordinateMap,
      LongDataMap<List<Long>> referenceMap) {
    this.nodeGeometryMapper = new NodeGeometryMapper(coordinateMap, referenceMap);
    this.wayGeometryMapper = new WayGeometryMapper(coordinateMap, referenceMap);
    this.relationGeometryMapper = new RelationGeometryMapper(coordinateMap, referenceMap);
  }

  /** {@inheritDoc} */
  @Override
  public T apply(T entity) {
    if (entity instanceof Node node) {
      return (T) nodeGeometryMapper.apply(node);
    } else if (entity instanceof Way way) {
      return (T) wayGeometryMapper.apply(way);
    } else if (entity instanceof Relation) {
      return (T) relationGeometryMapper.apply((Relation) entity);
    } else {
      return entity;
    }
  }

}
