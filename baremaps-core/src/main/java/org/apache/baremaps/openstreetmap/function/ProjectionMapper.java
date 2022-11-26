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
import org.apache.baremaps.openstreetmap.model.Element;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.utils.ProjectionTransformer;

/** A function that changes the projection of the geometry of an entity. */
public class ProjectionMapper<T extends Entity> implements Function<T, T> {

  private final ProjectionTransformer projectionTransformer;

  /**
   * Constructs a function that changes the projection of the geometry of an entity.
   *
   * @param projectionTransformer The projection transformer.
   */
  public ProjectionMapper(ProjectionTransformer projectionTransformer) {
    this.projectionTransformer = projectionTransformer;
  }

  /** {@inheritDoc} */
  @Override
  public T apply(T entity) {
    if (entity instanceof Element element && element.geometry() != null) {
      var geometry = projectionTransformer.transform(element.geometry());
      return (T) element.withGeometry(geometry);
    } else {
      return entity;
    }
  }

}
