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



import java.util.function.Function;
import org.apache.baremaps.openstreetmap.OsmReaderContext;
import org.apache.baremaps.openstreetmap.model.*;

public class EntityGeometryMapper<T extends Entity> implements Function<T, T> {

  private final OsmReaderContext context;

  public EntityGeometryMapper(OsmReaderContext context) {
    this.context = context;
  }

  @Override
  public T apply(T entity) {
    if (entity instanceof Node node) {
      return (T) new NodeGeometryMapper(context).apply(node);
    } else if (entity instanceof Way way) {
      return (T) new WayGeometryMapper(context).apply(way);
    } else if (entity instanceof Relation) {
      return (T) new RelationGeometryMapper(context).apply((Relation) entity);
    } else {
      return entity;
    }
  }

}
